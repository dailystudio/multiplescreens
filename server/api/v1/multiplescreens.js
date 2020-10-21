const fs            = require('fs');
const logger        = require('devbricksx-js').logger;
const requtils      = require('devbricksx-js').requtils;
const resputils     = require('devbricksx-js').resputils;
const constants     = require('../../modules/constants.js');
const wsmgr         = require('../../modules/wsmgr.js');

module.exports = {

    list: async function (req, res) {
        logger.info(`${__function}: sid = [${req.query[constants.PARAM_SESSION_ID]}]`);

        let sid = req.query[constants.PARAM_SESSION_ID];

        let clients = await wsmgr.list(sid);
        let response = {
            code: 200,
            screens: clients
        };

        res.end(JSON.stringify(response));
    },

};
