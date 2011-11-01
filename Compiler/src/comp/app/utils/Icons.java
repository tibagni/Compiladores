package comp.app.utils;

import javax.swing.ImageIcon;

/**
 *
 * @author Felps
 * Classe de padronização dos ícones da interface gráfica
 * Apenas para manter a organização
 */
public abstract class Icons {

    public static final int SAVE_ICON = 0;
    public static final int OPEN_ICON = 1;
    public static final int COMPILE_ICON = 2;
    public static final int NEW_ICON = 3;

    private static ImageIcon saveIcon = new ImageIcon("res\\icons\\save.gif");
    private static ImageIcon openIcon = new ImageIcon("res\\icons\\open.png");
    private static ImageIcon compileIcon = new ImageIcon("res\\icons\\compile.gif");
    private static ImageIcon newIcon = new ImageIcon("res\\icons\\novo.gif");

    public static ImageIcon getIcon(int icone) {
        if(icone == SAVE_ICON){
            return saveIcon;
        } else if(icone == OPEN_ICON) {
            return openIcon;
        } else if(icone == COMPILE_ICON) {
            return compileIcon;
        } else if (icone == NEW_ICON){
            return newIcon;
        }
        return null;
    }

}
