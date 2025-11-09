# AWS SES Email Setup Guide

This guide explains how to configure AWS Simple Email Service (SES) for sending invoice notifications in the InvoiceMe application.

## Overview

InvoiceMe uses AWS SES to send emails when:
- An invoice is marked as "Sent" (customer notification)
- A payment is recorded (payment confirmation)
- An invoice is fully paid (completion notification)
- An invoice becomes overdue (reminder with late fee notice)

## 1. Create AWS IAM Credentials

1. **Go to AWS IAM Console**: https://console.aws.amazon.com/iam/
2. Click **Users** → **Create user**
3. Name it something like `invoiceme-ses-user`
4. Click **Next** → Select **Attach policies directly**
5. Search for and select: `AmazonSESFullAccess`
6. Click **Next** → **Create user**
7. Click on the new user → **Security credentials** tab
8. Click **Create access key**
9. Choose **Application running outside AWS** → **Next**
10. **Save both** (you won't be able to see the secret key again):
   - **Access key ID** (e.g., `AKIAIOSFODNN7EXAMPLE`)
   - **Secret access key** (e.g., `wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY`)

⚠️ **Keep these credentials secure!** Never commit them to git.

## 2. Verify Email Addresses in SES (Sandbox Mode)

By default, AWS SES starts in **sandbox mode**, which means you must verify both sender and recipient email addresses.

### Verify the Sender Email

1. **Go to AWS SES Console**: https://console.aws.amazon.com/ses/
2. Make sure you're in the **us-east-1** region (top-right dropdown)
3. In the left sidebar, click **Verified identities**
4. Click **Create identity**
5. Select **Email address**
6. Enter: `mylesethan93@gmail.com` (or your desired sender email)
7. Click **Create identity**
8. **Check your email inbox** - you'll get a verification email from AWS
9. Click the verification link in the email
10. Wait for status to change to **Verified**

### Verify Recipient Emails (While in Sandbox Mode)

You must also verify every customer email address you want to send invoices to:

1. Repeat steps 4-10 above for each customer email address
2. Customer must check their inbox and click the verification link

**Example**: If you have a test customer with email `customer@example.com`, you must verify that email before sending them an invoice.

## 3. Set Environment Variables

### For Local Development

Create or update `/backend/.env`:

```env
# AWS Configuration
AWS_REGION=us-east-1
AWS_ACCESS_KEY_ID=AKIAIOSFODNN7EXAMPLE
AWS_SECRET_ACCESS_KEY=wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY
AWS_SES_FROM_EMAIL=mylesethan93@gmail.com
```

**Note**: The `.env` file is in `.gitignore` and will not be committed.

### For Production (AWS Elastic Beanstalk)

1. Go to **Elastic Beanstalk Console**
2. Select your environment (e.g., `invoiceme-mlx-env`)
3. Click **Configuration** → **Software** → **Edit**
4. Scroll to **Environment properties**
5. Add/update the following:

| Name | Value |
|------|-------|
| `AWS_REGION` | `us-east-1` |
| `AWS_ACCESS_KEY_ID` | `your-access-key-id` |
| `AWS_SECRET_ACCESS_KEY` | `your-secret-access-key` |
| `AWS_SES_FROM_EMAIL` | `mylesethan93@gmail.com` |

6. Click **Apply**
7. Wait for environment to update (2-3 minutes)

### For Vercel Frontend (if needed)

1. Go to **Vercel Dashboard** → Your project
2. **Settings** → **Environment Variables**
3. Add the same AWS variables
4. Click **Save**
5. Redeploy the frontend

## 4. Testing Email Functionality

### Quick Test

1. **Restart your backend** to load the new environment variables
2. **Create a test customer** with a **verified email address**
   - Use `mylesethan93@gmail.com` if you verified it
3. **Create an invoice** for this customer
4. Click **Mark as Sent** or use the post-creation modal's "Send to Customer" button
5. **Check the email inbox** (and spam folder)

### Check Backend Logs

Look for these log messages:

```
✅ Success:
INFO: Invoice email sent to customer@example.com for invoice INV-2025-0001

❌ Failure:
ERROR: Failed to send invoice email to customer@example.com
       Message rejected: Email address is not verified
```

### Common Issues

**Problem**: "Email address is not verified"
- **Solution**: Verify the recipient email in AWS SES Console (step 2)

**Problem**: "Invalid AWS credentials"
- **Solution**: Double-check `AWS_ACCESS_KEY_ID` and `AWS_SECRET_ACCESS_KEY` in environment variables

**Problem**: "Region not found"
- **Solution**: Ensure `AWS_REGION=us-east-1` is set

## 5. Move Out of Sandbox Mode (Production)

Once you're ready to send emails to any customer (without verifying each email), request production access:

1. Go to **SES Console** → **Account dashboard**
2. Click **Request production access** button
3. Fill out the form:
   - **Mail type**: Transactional
   - **Website URL**: Your production URL
   - **Use case description**:
     ```
     InvoiceMe is an invoicing system that sends transactional emails to customers:
     - Invoice notifications when invoices are created
     - Payment confirmations when payments are recorded
     - Payment reminders for overdue invoices
     
     All emails are sent only to customers who have an active business relationship
     with the sender. No marketing or promotional emails are sent.
     ```
   - **Process for handling bounces**: We monitor bounce notifications and update customer records
   - **Compliance**: We comply with CAN-SPAM and similar regulations
4. Submit the request
5. **Usually approved within 24 hours**

### After Approval

- You can send to **any email address** without verification
- Daily sending quota increases (typically 50,000 emails/day)
- Monitor your **reputation metrics** in SES Console to maintain good standing

## 6. Email Templates

Current email templates are in: `backend/src/main/java/com/invoiceme/infrastructure/email/AwsSesEmailService.java`

### Invoice Sent Email
```
Subject: Invoice [INV-2025-0001] from InvoiceMe

Dear Customer,

Your invoice INV-2025-0001 has been sent. Please find the invoice PDF attached.

Payment Link: https://invoiceme.com/pay/{invoiceId}

Thank you for your business!
```

### Payment Confirmation
```
Subject: Payment Received - Invoice [INV-2025-0001]

Dear Customer,

We have received your payment of $100.00 for invoice INV-2025-0001.

Thank you!
```

### Invoice Paid in Full
```
Subject: Invoice Paid in Full - [INV-2025-0001]

Dear Customer,

Your invoice INV-2025-0001 has been paid in full (Total: $100.00).

Thank you for your payment!
```

### Overdue Reminder
```
Subject: Payment Reminder - Invoice [INV-2025-0001] Overdue

Dear Customer,

Your invoice INV-2025-0001 is overdue. A late fee of $25.00 has been applied.

New Balance: $125.00

Please make payment as soon as possible.
```

## 7. Monitoring and Costs

### Monitor Sending Activity

1. Go to **SES Console** → **Account dashboard**
2. View metrics:
   - Emails sent
   - Bounces
   - Complaints
   - Opens (if tracking enabled)

### Cost Estimates

AWS SES Pricing (as of 2024):
- **First 62,000 emails/month**: FREE (if sending from EC2)
- **Additional emails**: $0.10 per 1,000 emails

**Example**:
- 500 invoices/month = FREE
- 100,000 invoices/month = ~$4/month

## 8. Security Best Practices

1. ✅ **Never commit AWS credentials** to git (use `.env` and `.gitignore`)
2. ✅ **Rotate credentials** every 90 days
3. ✅ **Use IAM role** for EC2/Elastic Beanstalk instead of access keys (more secure)
4. ✅ **Enable CloudWatch logs** for SES to track email delivery
5. ✅ **Set up SNS notifications** for bounces and complaints
6. ✅ **Monitor bounce rate** (keep below 5% to maintain good reputation)

## Need Help?

- **AWS SES Documentation**: https://docs.aws.amazon.com/ses/
- **Sandbox Mode Limits**: https://docs.aws.amazon.com/ses/latest/dg/request-production-access.html
- **Email Best Practices**: https://docs.aws.amazon.com/ses/latest/dg/send-email-concepts-deliverability.html

---

**Status**: Email functionality is implemented but requires AWS SES setup to be functional.

