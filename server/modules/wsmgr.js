const logger        = require('devbricksx-js').logger;

EXPOSE_PROPERTIES = [
    'uuid', 'sid', 'widthInDp', 'heightInDp', 'seq',
    'gridWidthInDp', 'gridHeightInDp',
    'xOffsetInDp', 'yOffsetInDp',
];

let clients = new Map();

module.exports = {

    register: function (uuid, ws) {
        if (uuid == null || ws == null) {
            return;
        }

        clients.set(uuid, ws);
        logger.debug(`new client [${uuid}] is registered.`);
    },


    unregister: function (uuid) {
        logger.debug(`unregister client ${uuid}`);
        if (uuid == null) {
            return;
        }

        clients.delete(uuid);
        logger.debug(`client [${uuid}] is unregistered.`);
    },

    get: function (uuid) {
        let client = clients.get(uuid);
        if (!client) {
            return undefined;
        }

        return dumpClient(client);
    },

    getWs: function (uuid) {
        return clients.get(uuid);
    },

    list: function (sid) {
        logger.debug(`find clients with sid: ${sid}`);
        let filtered = [];

        for (let [uuid, client] of clients) {
            logger.debug(`uuid: ${uuid}`);

            logger.debug(`client.sid: ${client.sid}`);

            if (sid == null || sid === client.sid) {
                filtered.push(dumpClient(client));
            }
        }

        return filtered;
    }

};


function copy(obj, include=[]) {
    return Object.keys(obj).reduce((target, k) => {
        if (include.indexOf(k) > -1) {
            target[k] = obj[k];
        }

        return target;
    }, {});
}

function dumpClient(client) {
    return copy(client, EXPOSE_PROPERTIES)
}
