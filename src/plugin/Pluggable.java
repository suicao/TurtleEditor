package plugin;

import gui.MainEditor;

/**
 * Created by khoi on 4/6/2014.
 */
public interface Pluggable {
    /**
     * Plugin khởi tạo phương thức tương tác được với người dùng
     * Plugin gọi load() trong constructor
     * */
    public void load();
    /**
     *Plugin loại bỏ phương thức tương tác người dùng
     * */
    public void unload();
    /**
     *Khởi chạy plugin, tự động hoặc cần kích hoạt
     * */
    public void run();
}
