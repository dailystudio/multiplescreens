const express      = require('express');
const multiplescreensV1   = require('./v1/multiplescreens.js');

const multiplescreensApiRouter = express.Router({});

multiplescreensApiRouter.get('/v1/multiplescreens/list', (req, res) => {
    return multiplescreensV1.list(req, res);
});

module.exports = multiplescreensApiRouter;
