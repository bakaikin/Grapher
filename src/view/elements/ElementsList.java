package view.elements;

import model.IntFunc;
import model.ViewElement;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

public class ElementsList extends ViewElement {
    private final JLabel name;
    private final JTextField state;
    private final JButton btn_make_element;
    private final ArrayList<TextElement> elements;
    private int height;
    private static final int MAX_SIZE = 10;
    public static final int OFFSET = 5;
    private static final int NAME_HEIGHT = 30;
    public static final int WIDTH = TextElement.WIDTH + 2 * OFFSET;
    private final Point pos;
    private Container c;
    public ElementsList(int x, int y, ActionListener sizeChanged, IntFunc settings) {
        elements = new ArrayList<>();
        pos = new Point();
        this.name = new JLabel();
        this.name.setFont(name_font);
        btn_make_element = new JButton("+");
        state = new JTextField();
        state.setEditable(false);
        state.setFont(new Font("arial", Font.PLAIN, 12));
        setBounds(x,y);
        btn_make_element.addActionListener((e)->{
            int y2 = height - OFFSET - TextElement.HEIGHT;
            TextElement el = new TextElement(x + OFFSET, y2);
            el.addRemoveListener((e2)->{
                int id = elements.indexOf(el);
                elements.remove(id);
                el.removeFrom(c);
                setBounds(pos.x, pos.y);
                sizeChanged.actionPerformed(new ActionEvent(0, id, "remove"));
                if(elements.size() == MAX_SIZE - 1){
                    c.add(btn_make_element);
                }
            });
            el.addSettingsListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if(e.getButton() == MouseEvent.BUTTON3){
                        settings.execute(elements.indexOf(el));
                    }
                }
            });
            int id = elements.size();
            elements.add(el);
            el.addTo(c);
            setBounds(pos.x, pos.y);
            sizeChanged.actionPerformed(new ActionEvent(0, id, "add"));
            if(elements.size() == MAX_SIZE){
                c.remove(btn_make_element);
            }
        });
    }
    public void setName(String name){
        this.name.setText(name);
    }
    public void addTo(Container c){
        c.add(name);
        c.add(state);
        for(TextElement e: elements)
            e.addTo(c);
        c.add(btn_make_element);
        this.c = c;
    }
    public void setBounds(int x, int y){
        pos.setLocation(x,y);
        name.setBounds(x + OFFSET,y + OFFSET, TextElement.WIDTH, NAME_HEIGHT);
        state.setBounds(x + 2 * OFFSET + TextElement.WIDTH * 2 / 5, y + OFFSET, 3*TextElement.WIDTH / 5 - OFFSET, NAME_HEIGHT);
        height = 2 * OFFSET + NAME_HEIGHT;
        for(TextElement e: elements){
            e.setBounds(x + OFFSET, y + height);
            height += TextElement.HEIGHT + OFFSET;
        }
        btn_make_element.setBounds(x + OFFSET, height + y, TextElement.WIDTH, TextElement.HEIGHT);
        if(elements.size() != MAX_SIZE)
            height += OFFSET + TextElement.HEIGHT;
    }

    public int getHeight() {
        return height;
    }

    public ArrayList<TextElement> getElements(){
        return elements;
    }

    public void setState(String text){
        this.state.setText(text);
    }
}
