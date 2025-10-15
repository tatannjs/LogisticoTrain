# LogisticoTrain - Front application

Front web application for the LogisticoTrain system

## Requirements 
- node >= 22 with npm ≥ 10.5

## Build for production

- Install the project dependencies: `npm ci` or `npm install`
- Prepare the configuration (see next section)
- run the build: `npm run build`
- expose the files in the created build/ folder on your server (you can remove the buildInfos/ subfolder if you want)

## Application configuration

3 informations have to be provided for the build: the application public URL, the REST API public URL, the Realtime API public URL.
There is two way to provided these information:

### Environment variable
You can use the next 3 environment variables for runing the build:

- __PUBLIC_PATH__: the public URL path of the application on your server, accessible from the client
- __API_BASE_URL__: the public URL path to the REST Api of the system. if it is served on the same server than this application, the path can be relative to the server root URL (eg.: "/rest/api")
- __RT_API_BASE_URL__: the public URL path to the Realtime Api of the system. if it is served on the same server than this application, the path can be relative to the server root URL (eg.: "/rest/api")

You can otherwise update the web the 3 following lines in the `weppack.prod.js` configuration file:
  - __l.13__ - PUBLIC_PATH: the public URL path of the application on your server, accessible from the client
  - __l.14__ - API_BASE_URL: the public URL path to the REST Api of the system. if it is served on the same server than this application, the path can be relative to the server root URL (eg.: "/rest/api")
  - __l.15__ RT_API_BASE_URL: the public URL path to the Realtime Api of the system. if it is served on the same server than this application, the path can be relative to the server root URL (eg.: "/rest/api")