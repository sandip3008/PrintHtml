var exec = require('cordova/exec');

exports.new_activity = function (data, success, error) {
    exec(success, error, 'PrintHtml', 'new_activity', [data]);
};
