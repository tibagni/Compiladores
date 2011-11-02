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
    public static final int LOADING_ICON = 4;

    private static final ImageIcon saveIcon = new ImageIcon("res\\icons\\save.gif");
    private static final ImageIcon openIcon = new ImageIcon("res\\icons\\open.png");
    private static final ImageIcon compileIcon = new ImageIcon("res\\icons\\compile.gif");
    private static final ImageIcon newIcon = new ImageIcon("res\\icons\\novo.gif");
    private static final ImageIcon loadingIcon = new ImageIcon("res\\icons\\loading.gif");

    public static ImageIcon getIcon(int icon) {
        switch (icon) {
            case SAVE_ICON:
                return saveIcon;
            case OPEN_ICON:
                return openIcon;
            case COMPILE_ICON:
                return compileIcon;
            case NEW_ICON:
                return newIcon;
            case LOADING_ICON:
                return loadingIcon;
            default:
                return null;
        }
    }

}
