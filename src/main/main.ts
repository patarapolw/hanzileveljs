import { app, BrowserWindow, Menu, MenuItemConstructorOptions } from "electron";
import path from "path";
import url from "url";
import isAsar from "electron-is-running-in-asar";
import { spawn, ChildProcess } from "child_process";
import getPort from "get-port";

let mainWindow: Electron.BrowserWindow;
let jProcess: ChildProcess;

app.on("ready", () => {
    mainWindow = new BrowserWindow({
        height: 768,
        width: 1024,
        webPreferences: {
            nodeIntegration: true
        }
    });
    mainWindow.maximize();

    mainWindow.loadURL(
        url.format({
            pathname: path.join(__dirname, "./index.html"),
            protocol: "file:",
            slashes: true
        })
    );

    if (!isAsar()) {
        mainWindow.webContents.openDevTools();
    } else {
        getPort().then((port) => {
            jProcess = spawn("java", ["-jar", path.join(__dirname, "../../hanzileveljs-all.jar")], {
                env: {
                    PORT: port.toString()
                }
            });
        });
    }

    const template: MenuItemConstructorOptions[] = [
        {
            label: "Application",
            submenu: [
                { label: "About Application", role: "about" },
                { type: "separator" },
                { label: "Quit", accelerator: "Command+Q", role: "quit" }
            ]
        },
        {
            label: "Edit",
            submenu: [
                { label: "Undo", accelerator: "CmdOrCtrl+Z", role: "undo" },
                { label: "Redo", accelerator: "Shift+CmdOrCtrl+Z", role: "redo" },
                { type: "separator" },
                { label: "Cut", accelerator: "CmdOrCtrl+X", role: "cut" },
                { label: "Copy", accelerator: "CmdOrCtrl+C", role: "copy" },
                { label: "Paste", accelerator: "CmdOrCtrl+V", role: "paste" },
                { label: "Select All", accelerator: "CmdOrCtrl+A", role: "selectAll" }
            ]
        }
    ];

    Menu.setApplicationMenu(Menu.buildFromTemplate(template));
});

app.on("window-all-closed", () => {
    // if (process.platform !== 'darwin') {
    app.quit();
    // }
});

app.on("quit", () => {
    if (jProcess) {
        jProcess.kill();
    }
});
