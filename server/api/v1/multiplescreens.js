const fs        = require('fs');
const multer    = require('multer');
const logger    = require('devbricksx-js').logger;

const UPLOAD_DIR = './uploads';

if (!fs.existsSync(UPLOAD_DIR)) {
    fs.mkdirSync(UPLOAD_DIR)
}

let uploadStorage = multer.diskStorage({

    destination: function (req, file, callback) {
        callback(null, UPLOAD_DIR);
    },

    filename: function (req, file, callback) {
        callback(null, file.originalname);
    }

});

let uploadHelper = multer({storage: uploadStorage}).single('file');

module.exports = {

    echo: function (req, res) {
        logger.info(`${__function}: echo message [${req.query.message}]`);
        let message = req.query.message;
        if (!message) {
            let error = {
                code: 400,
                message: 'missing parameters: message'
            };

            res.status(400).send(JSON.stringify(error));

            return;
        }

        let response = {
            code: 200,
            message: `${message}`
        };

        res.end(JSON.stringify(response));
    },

    echoUpload: function (req, res) {
        uploadHelper(req, res, function (err) {
            logger.info(`${__function}: [${JSON.stringify(req.file)}]`);
            if (err) {
                let error = {
                    code: 400,
                    message: 'error uploading file.' + err
                };

                res.status(400).send(JSON.stringify(error));

                return;
            }

            if (!req.file || !req.file.path) {
                let error = {
                    code: 400,
                    message: 'no input at file'
                };

                res.status(400).send(JSON.stringify(error));

                return;
            }

            let response = {
                code: 200,
                message: `file [${req.file.filename}] has been uploaded.`
            };

            res.end(JSON.stringify(response));
        });
    },

};
