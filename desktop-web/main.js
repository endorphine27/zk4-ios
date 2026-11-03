const { app, BrowserWindow, Menu, shell } = require('electron');

const APP_URL = 'https://destinyab.go.ro:8444/webos/desktop/';

app.commandLine.appendSwitch('autoplay-policy', 'no-user-gesture-required');

function createWindow() {
  const win = new BrowserWindow({
    width: 1440,
    height: 900,
    minWidth: 980,
    minHeight: 640,
    backgroundColor: '#0a0a0c',
    title: 'CINEBOX',
    titleBarStyle: 'hiddenInset',
    webPreferences: { contextIsolation: true, nodeIntegration: false, sandbox: true },
  });
  win.loadURL(APP_URL);
  win.webContents.setWindowOpenHandler(({ url }) => { shell.openExternal(url); return { action: 'deny' }; });
  win.webContents.on('did-fail-load', (_e, code, desc, url, isMain) => {
    if (!isMain) return;
    win.loadURL('data:text/html;charset=utf-8,' + encodeURIComponent(
      '<body style="background:#0a0a0c;color:#fff;font-family:-apple-system,Arial;display:flex;align-items:center;justify-content:center;height:100vh;margin:0;text-align:center">' +
      '<div><h1 style="color:#e50914">CINEBOX</h1><p>Nu mă pot conecta la serverul CINEBOX (' + desc + ').</p>' +
      '<p><button onclick="location.href=\'' + APP_URL + '\'" style="padding:12px 24px;font-size:16px;border:0;border-radius:8px;background:#e50914;color:#fff;cursor:pointer">Reîncearcă</button></p></div></body>'));
  });
}

app.whenReady().then(() => {
  Menu.setApplicationMenu(Menu.buildFromTemplate([
    { role: 'appMenu' },
    { role: 'editMenu' },
    { label: 'Vizualizare', submenu: [{ role: 'reload' }, { role: 'togglefullscreen' }, { role: 'resetZoom' }, { role: 'zoomIn' }, { role: 'zoomOut' }] },
    { role: 'windowMenu' },
  ]));
  createWindow();
  app.on('activate', () => { if (BrowserWindow.getAllWindows().length === 0) createWindow(); });
});
app.on('window-all-closed', () => { if (process.platform !== 'darwin') app.quit(); });
