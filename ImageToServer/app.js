var express = require('express');
var app = express();
var bodyParser = require('body-parser');
var mongoose = require('mongoose');
var http = require('http');
var User = require('./modules/user_schema');

mongoose.connect('mongodb://localhost/testDB',{useNewUrlParser: true, useUnifiedTopology: true});

// 전송 용량을 늘리기 위해 limit 설정
app.use(bodyParser.urlencoded({limit: '50mb',extended: true}));
app.use(bodyParser.json({limit: '50mb'}));

var router = require('./routes/user')(app,User);

app.set('port',3000);
var server = http.createServer(app).listen(app.get('port'), () => {
    console.log("Express server has started on port " + app.get('port'));

});