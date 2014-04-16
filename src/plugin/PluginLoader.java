package plugin;
import gui.MainEditor;


import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**
 * Created by khoi on 4/6/2014.
 */
public class PluginLoader {
    ClassFinder classFinder = new ClassFinder();
    public PluginLoader(MainEditor editor){
        final List<Class<?>> pluginList = classFinder.findClassesImpmenenting(Pluggable.class, Pluggable.class.getPackage());
        for(Class<?> Plugin: pluginList){
            try{
                Plugin.getConstructor(MainEditor.class).newInstance(editor);
            }catch (IllegalAccessException|InstantiationException|NoSuchMethodException|InvocationTargetException iae){
                System.out.print(Plugin.getName()+" is unable to load.");
            }
        }
    }
}
