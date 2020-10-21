const url           = require('url');
const logger        = require('devbricksx-js').logger;
const wsmgr         = require('./wsmgr.js');
const constants     = require('./constants.js');

module.exports = function(app, httpsServer) {

    logger.debug(`app: ${app}, server: ${httpsServer}`);
    global.wsInstance = require('express-ws')(app, httpsServer);

    app.ws('/screen', function(ws, req) {
        logger.debug(`new client is connected [url: ${req.url}]`);
        let urlO = url.parse(req.url, true);

        let sid = urlO.query[constants.PARAM_SESSION_ID];
        let uuid = urlO.query[constants.PARAM_UUID];

        if (sid === undefined) {
            logger.warn(`parameter [${constants.PARAM_SESSION_ID}] is missing`);
            ws.close();
        }

        if (uuid === undefined) {
            logger.warn(`parameter [${constants.PARAM_UUID}] is missing`);
            ws.close();
        }

        ws.sid = sid;
        ws.uuid = uuid;
        wsmgr.register(uuid, ws);

        ws.on('message', function(msg) {
            logger.debug(`receive message: [${msg}]`);
            // ws.send('[WS]: ' + msg);
            try {
                let msgObj = JSON.parse(msg);
                switch (msgObj.cmd) {
                    default:
                        logger.warn(`unsupported cmd: [${msgObj.cmd}]`);
                        break;
                }
            } catch (err) {
                logger.warn(`invalid message [${msg}]: ${err}`);
            }
        });

        ws.on('close', function(ws, req) {
            logger.info(`${this.uuid} disconnected.`);

            wsmgr.unregister(this.uuid);
        });

    });

};
