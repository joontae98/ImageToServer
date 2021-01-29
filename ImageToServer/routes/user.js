module.exports = (app, User) => {

    app.post('/process/up', (req, res) => {
        var user = new User({name: req.body.name, image: req.body.image});
        user.save((err) => {
            if (err) {
                console.log(err);
            }
            res.json(user);
        });
    })
    app.post('/process/down', (req, res) => {
        User.findOne({name: req.body.name}).exec((err, users) => {
            if (err) {
                console.log(err);
            }
            res.send(users);
        })
    })
}