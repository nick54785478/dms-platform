const _7z = require('7zip-min');
_7z.unpack('d:\\桌面\\MonoRepository\\dms-workspace\\sheet-export-module.7z', 'd:\\桌面\\MonoRepository\\dms-workspace\\temp_extract', err => {
    if (err) {
        console.error('Error:', err);
    } else {
        console.log('Extraction complete');
    }
});
