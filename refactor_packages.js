const fs = require('fs');
const path = require('path');

function walk(dir) {
    let results = [];
    const list = fs.readdirSync(dir);
    list.forEach(file => {
        file = path.join(dir, file);
        const stat = fs.statSync(file);
        if (stat && stat.isDirectory()) {
            results = results.concat(walk(file));
        } else {
            if (file.endsWith('.java') || file.endsWith('.xml') || file.endsWith('.yml')) {
                results.push(file);
            }
        }
    });
    return results;
}

const files = walk('d:\\桌面\\MonoRepository\\dms-workspace\\template-service\\src');

files.forEach(file => {
    let content = fs.readFileSync(file, 'utf8');
    let newContent = content.replace(/com\.example\.demo/g, 'com.dms.template');
    if (content !== newContent) {
        fs.writeFileSync(file, newContent, 'utf8');
        console.log('Updated ' + file);
    }
});
