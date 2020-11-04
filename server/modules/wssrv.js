const url           = require('url');
const logger        = require('devbricksx-js').logger;
const wsmgr         = require('./wsmgr.js');
const constants     = require('./constants.js');

let gridInXAxis = constants.GRID_COLS;
let gridInYAxis = constants.GRID_ROWS;

let sessions = new Map();

module.exports = function(app, httpsServer) {

    logger.debug(`app: ${app}, server: ${httpsServer}`);
    global.wsInstance = require('express-ws')(app, httpsServer, {});

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
        ws.seq = applySeq(sid);
        wsmgr.register(uuid, ws);

        updateScreenInfo(ws);
        syncGrisMap(ws);

        ws.on('message', function(msg) {
            logger.debug(`receive message: [${msg}]`);
            // ws.send('[WS]: ' + msg);
            try {
                let msgObj = JSON.parse(msg);
                switch (msgObj.cmdCode) {
                    case constants.CMD_CODE_REPORT_SCREEN_INFO:
                        ws.widthInDp = msgObj.widthInDp;
                        ws.heightInDp = msgObj.heightInDp;
                        splitCanvas(ws.sid);
                        break;

                    case constants.CMD_CODE_START_DRAWING: {
                        let sid = msgObj.sid;

                        startDrawing(sid);

                        break;
                    }

                    case constants.CMD_CODE_PAUSE_DRAWING: {
                        let sid = msgObj.sid;

                        pauseDrawing(sid);

                        break;
                    }

                    case constants.CMD_CODE_STOP_DRAWING: {
                        let sid = msgObj.sid;

                        stopDrawing(sid);

                        break;
                    }

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

            stopDrawing(this.sid);

            wsmgr.unregister(this.uuid);
            splitCanvas(this.sid);
        });

    });

};

function applySeq(sid) {
    let clients = wsmgr.list(sid);
    logger.debug(`clients: ${JSON.stringify(clients)}`);
    return (clients.length);
}

function updateScreenInfo(ws) {
    let data = wsmgr.get(ws.uuid);

    data.cmdCode = constants.CMD_CODE_UPDATE_SCREEN_INFO;
    logger.info(`update screen info: ${JSON.stringify(data)}`);
    sendDataSafely(ws, data);
}

function syncGrisMap(ws) {
    let data = {
        cmdCode: constants.CMD_CODE_SYNC_GRIDS_MAP,
        uuid: ws.uuid,
        map: constants.GRIDS_MAP
    };

    logger.info(`grids map info: ${JSON.stringify(data)}`);
    sendDataSafely(ws, data);
}

function startDrawing(sid) {
    logger.debug(`start drawing: sid = ${sid}`);

    let session = sessions.get(sid);
    if (!session) {
        session = {
            sid: sid,
            drawingIndex: 0,
        };

        sessions.set(sid, session);
    }

    resumeDrawing(sid);
}

function resumeDrawing(sid) {
    let session = sessions.get(sid);
    if (!session) {
        return;
    }

    session.handler = setInterval(function () {
        broadcastDrawing(sid,
            constants.GRIDS_MAP[session.drawingIndex++]);

        if (session.drawingIndex >= constants.GRIDS_MAP.length) {
            stopDrawing(sid);
        }
    }, constants.DRAWING_INTERVAL);
}

function pauseDrawing(sid) {
    let session = sessions.get(sid);
    if (!session) {
        return;
    }

    if (session.handler) {
        clearInterval(session.handler);
    }
}

function stopDrawing(sid) {
    logger.debug(`stop drawing: sid = ${sid}`);

    pauseDrawing(sid);
    broadcastEnding(sid);

    sessions.delete(sid);
}

function broadcastEnding(sid) {
    logger.debug(`[${sid}] ending`)
    let clients = wsmgr.list(sid);

    for (let c of clients) {
        let ws = wsmgr.getWs(c.uuid);

        let data = {
            cmdCode: constants.CMD_CODE_END_DRAWING,
            sid: sid,
            uuid: ws.uuid,
        };

        logger.info(`endding: ${JSON.stringify(data)}`);
        sendDataSafely(ws, data);
    }
}

function broadcastDrawing(sid, point) {
    logger.debug(`[${sid}] drawing point: ${JSON.stringify(point)}`)
    let clients = wsmgr.list(sid);

    for (let c of clients) {
        let ws = wsmgr.getWs(c.uuid);

        let data = {
            cmdCode: constants.CMD_CODE_DRAW_POINT,
            sid: sid,
            uuid: ws.uuid,
            point: point
        };

        logger.info(`drawing point: ${JSON.stringify(data)}`);
        sendDataSafely(ws, data);
    }
}

function splitCanvas(sid) {
    let clients = wsmgr.list(sid);

    let totalWidthInDp = 0;
    let minHeightInDp = -1;
    for (let c of clients) {
        totalWidthInDp += c.widthInDp;
        if (minHeightInDp === -1) {
            minHeightInDp = c.heightInDp;
        } else if (minHeightInDp > c.heightInDp) {
            minHeightInDp = c.heightInDp
        }
    }

    let gridWidth = Math.round(totalWidthInDp / gridInXAxis);
    let gridHeight = Math.round(minHeightInDp / gridInYAxis);
    logger.debug(`totalWidthInDp: ${totalWidthInDp} , minHeightInDp: ${minHeightInDp}`);
    logger.debug(`gridInXAxis: ${gridInXAxis} , gridInYAxis: ${gridInYAxis}`);
    logger.debug(`grid: ${gridWidth} x ${gridHeight}`);

    let canvasOffsetXInDp = 0;
    for (let c of clients) {
        let ws = wsmgr.getWs(c.uuid);

        if (ws) {
            ws.canvasOffsetXInDp = canvasOffsetXInDp;
            ws.canvasOffsetYInDp = 0;
            ws.gridWidthInDp = gridWidth;
            ws.gridHeightInDp = gridHeight;
            ws.drawingBoundInDp = {
                left: canvasOffsetXInDp,
                top: 0,
                right: canvasOffsetXInDp + c.widthInDp,
                bottom: minHeightInDp
            };

            updateScreenInfo(ws);

            canvasOffsetXInDp += c.widthInDp;
        }
    }
}

function sendDataSafely(ws, data) {
    try {
        ws.send(JSON.stringify(data));
    } catch (e) {
        logger.error(`send data failed: ${e}`)
    }
}
