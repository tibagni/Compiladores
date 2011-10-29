package comp.app.utils;

import javax.swing.ImageIcon;

/**
 *
 * @author Felps
 * Classe de padronização dos ícones da interface gráfica
 * Apenas para manter a organização
 */
public abstract class Icones {

    public static final int SAVE_ICON = 0;
    public static final int OPEN_ICON = 1;
    public static final int COMPILE_ICON = 2;
    public static final int NEW_ICON = 3;

    private static ImageIcon saveIcon = new ImageIcon("C:\\Users\\Felps\\Documents\\"
                + "NetBeansProjects\\Teste\\src\\icones\\save.gif");
    private static ImageIcon openIcon = new ImageIcon("C:\\Users\\Felps\\Documents\\"
                + "NetBeansProjects\\Teste\\src\\icones\\open.png");
    private static ImageIcon compileIcon = new ImageIcon("C:\\Users\\Felps\\Documents\\"
                + "NetBeansProjects\\Teste\\src\\icones\\compile.gif");
    private static ImageIcon newIcon = new ImageIcon("C:\\Users\\Felps\\Documents\\"
                + "NetBeansProjects\\Teste\\src\\icones\\novo.gif");

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
