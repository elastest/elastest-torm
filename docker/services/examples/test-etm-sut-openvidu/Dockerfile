FROM openvidu/openvidu-server-kms:2.6.0
RUN apt-get update
RUN apt-get install -y git
RUN apt-get install -y nodejs npm
RUN apt-get install -y curl
RUN curl -sL https://deb.nodesource.com/setup_8.x | bash - \
    && apt-get install -y nodejs
RUN npm install -g @angular/cli@7.1.3
RUN npm install -g http-server
EXPOSE 4443
EXPOSE 5000
CMD echo 'run supervisord'; /usr/bin/supervisord & echo '##### BUILD OPENVIDU #####'; git clone https://github.com/OpenVidu/openvidu.git; cd openvidu; git checkout tags/v2.8.0; cd openvidu-browser; npm install; npm run build; npm link; cd ..; cd openvidu-testapp; echo 'run npm install'; npm install; npm link openvidu-browser; ng build --output-path ./dist; cd dist; openssl req -newkey rsa:2048 -new -nodes -x509 -days 3650 -subj '/CN=www.mydom.com/O=My Company LTD./C=US' -keyout key.pem -out cert.pem; echo '##### RUN OPENVIDU #####'; http-server -S -p 5000;
