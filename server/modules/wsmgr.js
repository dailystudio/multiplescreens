const logger        = require('devbricksx-js').logger;

clients = {};

module.exports = {

    register: function (uuid, ws) {
        if (uuid == null || ws == null) {
            return;
        }

        clients[uuid] = ws;
        logger.debug(`new client [${uuid}] is registered.`);
    },


    unregister: function (uuid) {
        logger.debug(`unregister client ${uuid}`);
        if (uuid == null) {
            return;
        }

        delete clients[uuid];
        logger.debug(`client [${uuid}] is unregistered.`);
    },

    list: function (sid) {
        logger.debug(`find clients with sid: ${sid}`);
        return new Promise(function (resolve) {
            let filtered = [];

            let client;
            Object.keys(clients).forEach(function (uuid) {
                logger.debug(`uuid: ${uuid}`);
                client = clients[uuid];

                logger.debug(`client.sid: ${client.sid}`);

                if (sid == null || sid === client.sid) {
                    filtered.push({
                        "uuid": client.uuid,
                        "sid": client.sid
                    });
                }
            });

            resolve(filtered);
        })
    }

};
