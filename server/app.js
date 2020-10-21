const https         = require('https');
const fs            = require('fs');
const express       = require('express');
const bodyParser    = require('body-parser');
const cookieParser  = require('cookie-parser')();
const cors          = require('cors')({origin: true});
const logger        = require('devbricksx-js').logger;

const ENABLE_HTTPS = 'enable-https';
const KEY_PATH = 'key-path';
const CERT_PATH = 'cert-path';
const CERT_PASS_PHRASE = 'cert-pass-phrase';
const SERVER_PORT = 'server-port';

let argv = require('minimist')(process.argv.slice(2));

logger.enableDebugOutputs(argv);
logger.debug(`application arguments: ${JSON.stringify(argv, null, " ")}`);

const app = express();

let port = 1809;
if (argv[SERVER_PORT]) {
    port = argv[SERVER_PORT];
}

app.use(bodyParser.urlencoded({ extended: false }));
app.use(bodyParser.json());
app.use(cors);
app.use(cookieParser);
app.use(express.static(__dirname + '/public'));

app.use(require('./api/multiplescreensapi.js'));

if (argv[ENABLE_HTTPS]) {
    if (argv[CERT_PASS_PHRASE] == undefined) {
        logger.warn('no pass phrase input. use --cert-pass-phrase to set password of cert');
    }

    if (argv[KEY_PATH] == undefined) {
        logger.error('use --key-path to set path of key');
        process.exit(1);
    }

    if (argv[CERT_PATH] == undefined) {
        logger.error('use --cert-path to set path of cert');
        process.exit(1);
    }

    let options = {
        key: fs.readFileSync(argv[KEY_PATH]),
        cert: fs.readFileSync(argv[CERT_PATH]),
        passphrase: argv[CERT_PASS_PHRASE],
        requestCert: false,
        rejectUnauthorized: false
    };

    let server = https.createServer(options, app);
    let ws = require('./modules/wssrv.js')(app, server);

    server.listen(port, function(){
        logger.info(`Working on port ${port}, through HTTPS protocol`);
    });

} else {
    let ws = require('./modules/wssrv.js')(app);

    app.listen(port, function () {
        logger.info(`Working on port ${port}, through HTTP protocol`);
    });
}

