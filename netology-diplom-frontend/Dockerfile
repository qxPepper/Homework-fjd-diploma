FROM node:14.15.4-alpine

EXPOSE 8081

COPY package.json ./
COPY package-lock.json ./

RUN npm install

COPY ./ ./

CMD ["npm", "serve"]
#CMD ["npm", "start"]