const express      = require('express');
const multiplescreensV1   = require('./v1/multiplescreens.js');

const multiplescreensApiRouter = express.Router({});

multiplescreensApiRouter.get('/v1/multiplescreens/echo', (req, res) => {
    return multiplescreensV1.echo(req, res);
});

multiplescreensApiRouter.post('/v1/multiplescreens/echoUpload', (req, res) => {
    return multiplescreensV1.echoUpload(req, res);
});

module.exports = multiplescreensApiRouter;
