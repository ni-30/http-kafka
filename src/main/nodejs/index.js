const path    = require("path");
const bodyParser = require('body-parser');
const express = require('express');
const config = require('../resources/config.json');
const router = require('./router');

var app = express();
app.use(bodyParser.json());
app.use(bodyParser.urlencoded({ extended: true }));

app.post(config.http.processor.path, function (req, res) {
    
    metadata = {
        produceTimestamp: parseInt(req.header('X-ProducerTimestamp')),
        consumeTimestamp: parseInt(req.header('X-ConsumerTimestamp')),
        groupId: parseInt(req.header('X-GroupId')),
        topic: parseInt(req.header('X-Topic')),
        partition: parseInt(req.header('X-Partition')),
        offset: parseInt(req.header('X-Offset')),
        routerPath: parseInt(req.header('X-Router'))
    };
    data = req.body;

    status = router.route(metadata, data, function(status) {
        switch (status) {
            case 'ok':
                res.sendStatus(200);
                break;
            case 'bad-route-path':
                res.sendStatus(400);
                break;
            default:
                res.sendStatus(500);
                break;
        };
    });
});

var server = app.listen(config.http.processor.port, config.http.host, function () {
   var host = server.address().address
   var port = server.address().port
   console.log("node processor listening at http://%s:%s", host, port);
});