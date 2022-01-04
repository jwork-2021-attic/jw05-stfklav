/*
 * Copyright (C) 2015 Aeranythe Echosong
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package screen;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;

import javax.swing.JOptionPane;

import asciiPanel.AsciiPanel;

/**
 *
 * @author Aeranythe Echosong
 */
public class StartScreen extends RestartScreen {

    public Screen reload() {
        try {
            FileInputStream fileInputStream = new FileInputStream(Screen.FileName);
        } catch (FileNotFoundException e) {
            // TODO: handle exception
            System.out.println("不存在游戏保存文件");
            return this;
        }

        // 弹出提示
        int res = JOptionPane.showConfirmDialog(null, "是否恢复之前保存的游戏", "Option", JOptionPane.YES_NO_OPTION);
        if (res == JOptionPane.YES_OPTION) {
            // 点击“是”后执行这个代码块
            // 恢复
            try {
                return deserializing();
            } catch (Exception e) {
                // e.printStackTrace();
                System.out.println("反序列化失败！");
            }
        } else {
            // 点击“否”后执行这个代码块
            // 开启新游戏
           return this;
        }
        return this;

    }

    // 反序列化
    public Screen deserializing() throws IOException, ClassNotFoundException {
        FileInputStream fileInputStream = new FileInputStream(Screen.FileName);
        ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
        PlayScreen p = (PlayScreen) objectInputStream.readObject();
        monster_num = p.MONSTER_NUMBER;
        fungus_num = p.FUNGUS_NUMBER;
        medicine_num = p.MEDICINE_NUMBER;
        amplifier_num = p.AMPLIFIER_NUMBER;
        level = p.level;
        p.run();
        fileInputStream.close();
        objectInputStream.close();
        return p;
    }

    @Override
    public Screen displayOutput(AsciiPanel terminal) {
        terminal.write("This is the start screen.", 0, 0);
        terminal.write("Press ENTER to continue...", 0, 2);
        return this;
    }

}
