const { writeFile } = require('fs');
const { argv } = require('yargs');

// read environment variables from .env file
require('dotenv').config();

// read the command line arguments passed with yargs
const environment = argv.environment;
console.log(require('dotenv').config());

const isProduction = environment === 'prod';

const targetPath = isProduction
   ? `./src/environments/environment.docker.ts`
   : `./src/environments/environment.ts`;

// we have access to our environment variables
// in the process.env object thanks to dotenv
const environmentFileContent = `
export const environment = {
   production: ${isProduction},
   SSO_LOGIN: ${process.env.SSO_LOGIN},
   baseUrl: "${process.env.BASEURL}"
};
`;

// write the content to the respective file
writeFile(targetPath, environmentFileContent, function (err) {
   if (err) {
      console.log(err);
   }

   console.log(`Wrote variables to ${targetPath}`);
});
