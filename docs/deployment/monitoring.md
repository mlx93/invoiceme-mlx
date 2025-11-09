# InvoiceMe Monitoring Guide

**Purpose**: Guide for configuring and using AWS CloudWatch for monitoring InvoiceMe backend and frontend applications.

---

## Table of Contents

1. [CloudWatch Logs](#cloudwatch-logs)
2. [Custom Metrics](#custom-metrics)
3. [Alarms](#alarms)
4. [Dashboards](#dashboards)
5. [Best Practices](#best-practices)

---

## CloudWatch Logs

### Backend Logs (Elastic Beanstalk)

Elastic Beanstalk automatically streams application logs to CloudWatch.

#### Log Groups

**Primary Log Group**:
- Name: `/aws/elasticbeanstalk/invoiceme-backend-prod/var/log/eb-engine.log`
- Contains: Application startup, runtime errors, Spring Boot logs

**Additional Log Groups** (if configured):
- `/aws/elasticbeanstalk/invoiceme-backend-prod/var/log/web.stdout.log` - Standard output
- `/aws/elasticbeanstalk/invoiceme-backend-prod/var/log/web.stderr.log` - Standard error
- `/aws/elasticbeanstalk/invoiceme-backend-prod/var/log/nginx/access.log` - Nginx access logs
- `/aws/elasticbeanstalk/invoiceme-backend-prod/var/log/nginx/error.log` - Nginx error logs

#### Viewing Logs

**Via AWS Console**:
1. Go to CloudWatch → Log groups
2. Select log group: `/aws/elasticbeanstalk/invoiceme-backend-prod/var/log/eb-engine.log`
3. Click on a log stream to view logs
4. Use filter patterns to search logs

**Via AWS CLI**:
```bash
# List log groups
aws logs describe-log-groups --log-group-name-prefix "/aws/elasticbeanstalk/invoiceme-backend"

# View recent log events
aws logs tail /aws/elasticbeanstalk/invoiceme-backend-prod/var/log/eb-engine.log --follow

# Filter logs
aws logs filter-log-events \
  --log-group-name "/aws/elasticbeanstalk/invoiceme-backend-prod/var/log/eb-engine.log" \
  --filter-pattern "ERROR" \
  --start-time $(date -u -d '1 hour ago' +%s)000
```

#### Log Retention Policy

**Configuration**:
- **Development**: 7 days
- **Production**: 30 days

**Set Retention**:
```bash
# Set retention to 30 days
aws logs put-retention-policy \
  --log-group-name "/aws/elasticbeanstalk/invoiceme-backend-prod/var/log/eb-engine.log" \
  --retention-in-days 30
```

**Via Console**:
1. Go to CloudWatch → Log groups
2. Select log group
3. Actions → Edit retention
4. Select retention period → Save

### Frontend Logs (Amplify)

Amplify provides build and deployment logs.

#### Viewing Logs

**Via AWS Console**:
1. Go to Amplify → App → Deployments
2. Select deployment
3. Click "View logs"

**Log Types**:
- Build logs: npm install, build errors
- Deploy logs: Deployment status, CDN invalidation

#### Log Retention

- Build logs: Retained for 30 days
- Deploy logs: Retained for 30 days

---

## Custom Metrics

### Application Metrics

#### API Latency Metrics

**Purpose**: Track API response times (p50, p95, p99)

**Implementation** (optional, can be added to application code):

```java
// Example: Add to controller
@RestController
public class CustomerController {
    
    private final CloudWatchMetricPublisher metricPublisher;
    
    @GetMapping("/customers")
    public ResponseEntity<?> listCustomers() {
        long startTime = System.currentTimeMillis();
        try {
            // ... handler logic ...
            long duration = System.currentTimeMillis() - startTime;
            metricPublisher.publishMetric("ApiLatency", duration, "Endpoint", "/customers");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            metricPublisher.publishMetric("ApiErrors", 1, "Endpoint", "/customers");
            throw e;
        }
    }
}
```

**Metrics to Track**:
- `ApiLatency` - Response time in milliseconds
- `ApiErrors` - Error count per endpoint
- `RequestCount` - Total requests per endpoint

#### Database Connection Pool Metrics

**Purpose**: Monitor database connection pool usage

**Metrics**:
- `DatabasePoolActive` - Active connections
- `DatabasePoolIdle` - Idle connections
- `DatabasePoolTotal` - Total connections

**Implementation** (via Spring Boot Actuator):
```yaml
# application.yml
management:
  metrics:
    export:
      cloudwatch:
        namespace: InvoiceMe/Backend
        enabled: true
```

#### Business Metrics

**Purpose**: Track business-specific metrics

**Metrics**:
- `InvoiceCreated` - Count of invoices created
- `PaymentRecorded` - Count of payments recorded
- `EmailSent` - Count of emails sent
- `PdfGenerated` - Count of PDFs generated

### Publishing Custom Metrics

**Via AWS SDK** (Java):

```java
import software.amazon.awssdk.services.cloudwatch.CloudWatchClient;
import software.amazon.awssdk.services.cloudwatch.model.*;

public class CloudWatchMetricPublisher {
    
    private final CloudWatchClient cloudWatch;
    private final String namespace = "InvoiceMe/Backend";
    
    public void publishMetric(String metricName, double value, String... dimensions) {
        Dimension.Builder dimensionBuilder = Dimension.builder()
            .name("Environment")
            .value("production");
        
        MetricDatum metricDatum = MetricDatum.builder()
            .metricName(metricName)
            .value(value)
            .timestamp(Instant.now())
            .dimensions(dimensionBuilder.build())
            .unit(StandardUnit.NONE)
            .build();
        
        PutMetricDataRequest request = PutMetricDataRequest.builder()
            .namespace(namespace)
            .metricData(metricDatum)
            .build();
        
        cloudWatch.putMetricData(request);
    }
}
```

**Via AWS CLI**:

```bash
aws cloudwatch put-metric-data \
  --namespace "InvoiceMe/Backend" \
  --metric-data MetricName=InvoiceCreated,Value=1,Unit=Count
```

---

## Alarms

### Backend Alarms

#### API Error Rate Alarm

**Purpose**: Alert when API error rate exceeds threshold

**Configuration**:
- **Metric**: `4xx` + `5xx` errors / total requests
- **Threshold**: Error rate > 5%
- **Evaluation Period**: 5 minutes
- **Datapoints**: 2 out of 3

**Create Alarm**:
```bash
aws cloudwatch put-metric-alarm \
  --alarm-name "InvoiceMe-Backend-HighErrorRate" \
  --alarm-description "Alert when API error rate exceeds 5%" \
  --metric-name "4xxError" \
  --namespace "AWS/ApplicationELB" \
  --statistic "Sum" \
  --period 300 \
  --evaluation-periods 3 \
  --threshold 5 \
  --comparison-operator "GreaterThanThreshold" \
  --alarm-actions "arn:aws:sns:us-east-1:ACCOUNT_ID:InvoiceMeAlerts"
```

**Via Console**:
1. Go to CloudWatch → Alarms → Create alarm
2. Select metric: `AWS/ApplicationELB` → `HTTPCode_Target_4XX_Count`
3. Conditions:
   - Statistic: Sum
   - Period: 5 minutes
   - Threshold: > 5
4. Actions: Configure SNS topic for notifications

#### Database Connection Pool Exhausted Alarm

**Purpose**: Alert when database connection pool is near capacity

**Configuration**:
- **Metric**: `DatabasePoolActive` / `DatabasePoolTotal`
- **Threshold**: Utilization > 80%
- **Evaluation Period**: 5 minutes

**Create Alarm**:
```bash
aws cloudwatch put-metric-alarm \
  --alarm-name "InvoiceMe-Backend-DatabasePoolExhausted" \
  --alarm-description "Alert when database pool utilization exceeds 80%" \
  --metric-name "DatabasePoolUtilization" \
  --namespace "InvoiceMe/Backend" \
  --statistic "Average" \
  --period 300 \
  --evaluation-periods 2 \
  --threshold 80 \
  --comparison-operator "GreaterThanThreshold"
```

#### Health Check Failures Alarm

**Purpose**: Alert when health check endpoint fails

**Configuration**:
- **Metric**: `UnHealthyHostCount` (from Elastic Beanstalk)
- **Threshold**: > 0
- **Evaluation Period**: 2 minutes

**Create Alarm**:
```bash
aws cloudwatch put-metric-alarm \
  --alarm-name "InvoiceMe-Backend-HealthCheckFailure" \
  --alarm-description "Alert when health check fails" \
  --metric-name "UnHealthyHostCount" \
  --namespace "AWS/ElasticBeanstalk" \
  --dimensions Name=EnvironmentName,Value=invoiceme-backend-prod \
  --statistic "Maximum" \
  --period 60 \
  --evaluation-periods 2 \
  --threshold 0 \
  --comparison-operator "GreaterThanThreshold"
```

### Frontend Alarms

#### Build Failure Alarm

**Purpose**: Alert when Amplify build fails

**Configuration**:
- **Metric**: `BuildFailed` (from Amplify)
- **Threshold**: > 0
- **Evaluation Period**: 5 minutes

**Note**: Amplify doesn't expose CloudWatch metrics by default. Use GitHub Actions notifications or Amplify webhooks instead.

---

## Dashboards

### Backend Dashboard

**Purpose**: Visualize backend metrics in one place

**Metrics to Include**:
1. **API Performance**:
   - Request count (per endpoint)
   - Response time (p50, p95, p99)
   - Error rate (4xx, 5xx)

2. **Infrastructure**:
   - CPU utilization
   - Memory utilization
   - Network I/O

3. **Database**:
   - Connection pool utilization
   - Query latency
   - Connection errors

4. **Application**:
   - Invoice creation rate
   - Payment processing rate
   - Email send rate

**Create Dashboard**:

**Via Console**:
1. Go to CloudWatch → Dashboards → Create dashboard
2. Name: `InvoiceMe-Backend-Production`
3. Add widgets:
   - Line graph: Request count over time
   - Line graph: Response time (p95)
   - Number widget: Error rate percentage
   - Gauge: Database pool utilization

**Via CloudFormation** (optional):
```yaml
Resources:
  BackendDashboard:
    Type: AWS::CloudWatch::Dashboard
    Properties:
      DashboardName: InvoiceMe-Backend-Production
      DashboardBody: |
        {
          "widgets": [
            {
              "type": "metric",
              "properties": {
                "metrics": [
                  ["AWS/ApplicationELB", "RequestCount"]
                ],
                "period": 300,
                "stat": "Sum",
                "region": "us-east-1",
                "title": "Request Count"
              }
            }
          ]
        }
```

### Frontend Dashboard

**Metrics to Include**:
1. **Build Metrics**:
   - Build success rate
   - Build duration
   - Deployment frequency

2. **Performance**:
   - Page load time
   - API call latency
   - Error rate

**Note**: Frontend metrics are limited in Amplify. Consider using:
- Google Analytics for user metrics
- Sentry for error tracking
- Custom CloudWatch metrics via API calls

---

## Best Practices

### Logging Best Practices

1. **Structured Logging**:
   - Use JSON format for logs
   - Include correlation IDs for request tracing
   - Log at appropriate levels (ERROR, WARN, INFO, DEBUG)

2. **Sensitive Data**:
   - Never log passwords, tokens, or credit card numbers
   - Mask PII (personally identifiable information)
   - Use log filtering to exclude sensitive fields

3. **Log Levels**:
   - **ERROR**: Application errors, exceptions
   - **WARN**: Deprecated features, configuration issues
   - **INFO**: Business events (invoice created, payment recorded)
   - **DEBUG**: Detailed debugging information (development only)

### Metric Best Practices

1. **Naming Conventions**:
   - Use consistent naming: `ServiceName/MetricName`
   - Include dimensions for filtering
   - Use standard units (Count, Milliseconds, Bytes)

2. **Cardinality**:
   - Avoid high-cardinality dimensions (e.g., user IDs)
   - Use aggregation instead of individual metrics
   - Limit dimensions to 10 per metric

3. **Cost Optimization**:
   - Use standard CloudWatch metrics when possible
   - Batch custom metric publishing
   - Set appropriate retention periods

### Alarm Best Practices

1. **Threshold Selection**:
   - Base thresholds on historical data
   - Set different thresholds for dev/staging/prod
   - Review and adjust thresholds regularly

2. **Notification Channels**:
   - Use SNS topics for alerts
   - Configure multiple notification endpoints (email, Slack, PagerDuty)
   - Set up escalation policies

3. **Alarm Actions**:
   - Use auto-scaling for capacity issues
   - Use Lambda functions for automated remediation
   - Document runbooks for manual intervention

### Cost Optimization

1. **Log Retention**:
   - Set appropriate retention periods (7-30 days)
   - Archive old logs to S3 if needed
   - Use log filtering to reduce log volume

2. **Custom Metrics**:
   - Limit custom metrics to essential business metrics
   - Use standard metrics when possible
   - Batch metric publishing

3. **Dashboards**:
   - Limit dashboard refresh rate
   - Use appropriate time ranges
   - Archive unused dashboards

---

## Troubleshooting

### Logs Not Appearing

**Symptoms**: Logs don't appear in CloudWatch

**Solutions**:
1. **Check IAM Permissions**:
   - Verify Elastic Beanstalk has permission to write logs
   - Check IAM role: `aws-elasticbeanstalk-ec2-role`

2. **Check Log Configuration**:
   - Verify log streaming is enabled in Elastic Beanstalk
   - Check log group exists in CloudWatch

3. **Check Application Logging**:
   - Verify application is writing to stdout/stderr
   - Check log level configuration

### Metrics Not Updating

**Symptoms**: Custom metrics don't appear or update

**Solutions**:
1. **Check IAM Permissions**:
   - Verify application has `cloudwatch:PutMetricData` permission

2. **Check Metric Namespace**:
   - Verify namespace matches in code and CloudWatch

3. **Check Metric Publishing**:
   - Verify metric publishing code is executing
   - Check for exceptions in application logs

### Alarms Not Triggering

**Symptoms**: Alarms don't trigger when threshold is exceeded

**Solutions**:
1. **Check Alarm Configuration**:
   - Verify threshold is correct
   - Check evaluation periods and datapoints

2. **Check Metric Data**:
   - Verify metric data is being published
   - Check metric name and namespace match

3. **Check SNS Topic**:
   - Verify SNS topic exists and is subscribed
   - Test SNS topic manually

---

## Additional Resources

- [AWS CloudWatch Documentation](https://docs.aws.amazon.com/cloudwatch/)
- [Elastic Beanstalk Logging](https://docs.aws.amazon.com/elasticbeanstalk/latest/dg/using-features.logging.html)
- [Amplify Monitoring](https://docs.aws.amazon.com/amplify/latest/userguide/monitoring.html)
- [Spring Boot Actuator](https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html)

---

**Last Updated**: 2025-01-27  
**Maintained By**: DevOps Team

