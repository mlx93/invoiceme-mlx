# Frontend Deployment Issue Summary

## Issue Overview and Current State

The InvoiceMe frontend deployment to AWS Elastic Beanstalk is experiencing critical failures. When users attempt to login via the deployed frontend at `http://Invoiceme-mlx-frontend-env.eba-z7ur29fp.us-east-1.elasticbeanstalk.com`, they encounter a CORS error indicating the frontend is attempting to connect to `http://localhost:8080/api/v1/auth/login` instead of the actual backend API at `http://invoiceme-mlx-back-env.eba-jj8c3aur.us-east-1.elasticbeanstalk.com/api/v1`. This occurs because the `NEXT_PUBLIC_API_URL` environment variable was not properly baked into the frontend JavaScript bundle during the build process. In Next.js, `NEXT_PUBLIC_` prefixed environment variables are build-time variables that get compiled into the JavaScript code during `npm run build`, not runtime variables that can be changed after deployment. The frontend code defaults to `http://localhost:8080/api/v1` when this build-time variable is not set, which is why users see the localhost connection error despite the environment variable being correctly configured in the Elastic Beanstalk environment settings.

## Resolution Attempt and Subsequent Deployment Failures

To resolve the localhost connection issue, I identified that the GitHub Actions workflow was referencing a GitHub Secret `NEXT_PUBLIC_API_URL` during the frontend build step (line 97 of deploy.yml), but this secret was not configured in the repository. After the user added the secret with the correct backend URL (`http://invoiceme-mlx-back-env.eba-jj8c3aur.us-east-1.elasticbeanstalk.com/api/v1`), I triggered a rebuild via an empty commit (SHA: b92fc81) to bake the correct API URL into the JavaScript bundle. However, this deployment failed catastrophically with a 15-minute timeout during the Elastic Beanstalk deployment phase. The GitHub Actions logs show the deployment package was successfully uploaded to S3 and the application version was created, but the actual deployment to the environment timed out with "Command execution completed on all instances. Summary: [Successful: 0, TimedOut: 1]" and was eventually aborted by a user. The AWS health logs indicate "Incorrect application version" showing a mismatch between deployed version "frontend-b92fc81abb03accf0c3d97f4c4ec3c17140cd296-18" (deployment 3) and expected version "frontend-c3acecdc0fbb6f8f272d6f575801c9c89804b3ef-17" (deployment 2), with the final error being "Engine execution has encountered an error."

## Areas for Investigation

Several deployment-related issues emerged after setting the GitHub Secret that were not present in earlier deployments, suggesting the problem may not be directly related to the environment variable change itself but rather to accumulated state issues in the Elastic Beanstalk environment. First, investigate why the deployment is timing out during the command execution phase - the logs show the instance (i-0a57aacf01a7210e4) is not responding within the allowed timeout, which could indicate that `npm install` is hanging, the Node.js server is failing to start properly, or there's a port conflict preventing the application from binding to port 8080. Second, examine the "Incorrect application version" error which suggests the environment may have gotten into an inconsistent state from previous failed deployments, particularly from the initial timeout issues we encountered when the deployment package included the large `.next/dev/` folder. Third, check the CloudWatch logs (`/aws/elasticbeanstalk/Invoiceme-mlx-frontend-env/var/log/eb-engine.log` and `/var/log/nodejs/nodejs.log`) to see the actual Node.js startup errors, as the Procfile should be running `npm start` but something is preventing the application from starting successfully. It's worth noting that manual deployment of the same ZIP file might succeed where automated deployment fails, suggesting the issue could be related to GitHub Actions deployment timing, environment state, or the Elastic Beanstalk deployment mechanism itself rather than the application code or configuration.

---

## Technical Details for Investigation

### Deployment Configuration
- **Application**: invoiceme-mlx-frontend
- **Environment**: Invoiceme-mlx-frontend-env
- **Platform**: Node.js 20 on Amazon Linux 2023
- **Failed Version**: frontend-b92fc81abb03accf0c3d97f4c4ec3c17140cd296-18
- **Last Successful Version**: frontend-c3acecdc0fbb6f8f272d6f575801c9c89804b3ef-17

### Key Files
- **Workflow**: `.github/workflows/deploy.yml` (line 92-97 handles NEXT_PUBLIC_API_URL)
- **Procfile**: `frontend/Procfile` (contains: `web: npm start`)
- **API Configuration**: `frontend/src/lib/api.ts` (defaults to localhost:8080)

### Environment Variables
- **GitHub Secret**: NEXT_PUBLIC_API_URL = `http://invoiceme-mlx-back-env.eba-jj8c3aur.us-east-1.elasticbeanstalk.com/api/v1` (newly added)
- **Elastic Beanstalk**: NEXT_PUBLIC_API_URL = same value (set but ineffective for Next.js builds)
- **Elastic Beanstalk**: NODE_ENV = production

### Previous Working Deployments
- Deployments were successful before adding the GitHub Secret
- Frontend was accessible but connecting to wrong backend (localhost)
- No timeout issues with earlier deployments
- Manual deployment ZIP was successfully created: `frontend-manual-deploy.zip`

### Deployment Timeline
1. Initial timeouts due to large `.next/dev/` folder - RESOLVED by excluding dev artifacts
2. Environment name mismatches - RESOLVED (now using correct names)
3. Application name mismatch - RESOLVED (invoiceme-mlx-frontend)
4. GitHub Secret added for NEXT_PUBLIC_API_URL
5. **NEW ISSUE**: Deployment now timing out after Secret addition

### Recommended Next Steps
1. Check if environment is stuck in bad state (may need to terminate and recreate)
2. Review CloudWatch logs for actual Node.js errors
3. Test manual deployment of same ZIP file via console
4. Verify Procfile is working correctly
5. Consider if GitHub Secret addition caused build output changes affecting deployment
6. Check if there's a port binding issue or conflicting process

