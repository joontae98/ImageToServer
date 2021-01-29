var mongoose = require('mongoose');
var Schema = mongoose.Schema;

var userSchema = new Schema(
    {
        name: {type: String},
        image: {type: String },
    }
);
module.exports = mongoose.model('user',userSchema);