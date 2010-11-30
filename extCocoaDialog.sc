+ CocoaDialog {
    *selectDirectory { |startDir|
        var cmd;
        startDir = startDir ?? "echo $HOME".unixCmdGetStdOut.trim.trim($\n);
        cmd = "/Applications/CocoaDialog.app/Contents/MacOS/CocoaDialog fileselect --with-directory % --select-only-directories".format(startDir);
        ^(cmd.unixCmdGetStdOut.trim($\n));
        
    }
}
