/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package cms;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class RealDateTime {

    private static final DateTimeFormatter DATE_FORMAT
            = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static final DateTimeFormatter TIME_FORMAT
            = DateTimeFormatter.ofPattern("hh:mm a");

    public static String getFormattedDate() {
        return "Date : " + LocalDate.now().format(DATE_FORMAT);
    }

    public static String getFormattedTime() {
        return "Time : " + LocalTime.now().format(TIME_FORMAT);
    }
}
