FROM node:14.15.4-alpine

EXPOSE 8080

COPY package.json ./
COPY package-lock.json ./

RUN npm install

COPY ./ ./

CMD ["npm", "run", "serve"]
