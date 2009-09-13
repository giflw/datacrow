/******************************************************************************
 *                                     __                                     *
 *                              <-----/@@\----->                              *
 *                             <-< <  \\//  > >->                             *
 *                               <-<-\ __ /->->                               *
 *                               Data /  \ Crow                               *
 *                                   ^    ^                                   *
 *                              info@datacrow.net                             *
 *                                                                            *
 *                       This file is part of Data Crow.                      *
 *       Data Crow is free software; you can redistribute it and/or           *
 *        modify it under the terms of the GNU General Public                 *
 *       License as published by the Free Software Foundation; either         *
 *              version 3 of the License, or any later version.               *
 *                                                                            *
 *        Data Crow is distributed in the hope that it will be useful,        *
 *      but WITHOUT ANY WARRANTY; without even the implied warranty of        *
 *           MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.             *
 *           See the GNU General Public License for more details.             *
 *                                                                            *
 *        You should have received a copy of the GNU General Public           *
 *  License along with this program. If not, see http://www.gnu.org/licenses  *
 *                                                                            *
 ******************************************************************************/

package net.datacrow.util.isbn;



public abstract class ISBN {

    private static final int[] weight13 = {1, 3, 1, 3, 1, 3, 1, 3, 1, 3, 1, 3};
    private static final int[] weight10 = {1, 2, 3, 4, 5, 6, 7, 8, 9};
    
    public static String getISBN13(String isbn10) throws InvalidBarCodeException {
        String s = "978";
        char[] chars = isbn10.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            if (Character.isDigit(chars[i]))
                s += chars[i];
        }
        
        if (s.length() < 13)
            throw new InvalidBarCodeException();

        // remove the check digit
        int[] digits = new int[12];
        chars = s.toCharArray();
        for (int i = 0; i < digits.length; i++) {
            digits[i] = Integer.parseInt("" + chars[i]);
        }
        
        return convertToISBN13(digits);
    }
    
    public static String getISBN10(String isbn13) throws InvalidBarCodeException {
        char[] chars = isbn13.toCharArray();
        String s = "";
        for (int i = 0; i < chars.length; i++) {
            if (Character.isDigit(chars[i])) 
                s += chars[i];
        }
        
        
        if (s.length() < 10)
            throw new InvalidBarCodeException();

        int[] digits = new int[9];
        chars = s.toCharArray();
        int counter = 0;
        for (int i = 3; i < chars.length - 1; i++) {
            digits[counter++] = Integer.parseInt("" + chars[i]);
        }
        
        return convertToISBN10(digits);
    }    
    
    public static boolean isISBN13(String isbn) {
        String s = "";
        
        if (isbn != null) {
            char[] chars = isbn.toCharArray();
            for (int i = 0; i < chars.length; i++) {
                if ("1234567890".indexOf(chars[i]) > -1) 
                    s += chars[i];
            }
        }
        
        return s.length() == 13;
    }

    public static boolean isISBN10(String isbn) {
        String s = "";
        
        if (isbn != null) {
            char[] chars = isbn.toCharArray();
            for (int i = 0; i < chars.length; i++) {
                if ("1234567890".indexOf(chars[i]) > -1) 
                    s += chars[i];
            }
        }
        
        return s.length() == 10;
    }
    
    private static String convertToISBN10(int[] isbn13) {
        String isbn10 = "";
        
        int sum = 0;
        for (int i = 0; i < isbn13.length; i++)
            sum += isbn13[i] * weight10[i];
        
        int checkdigit = sum % 11;
        
        for (int i = 0; i < isbn13.length; i++)
            isbn10 += "" + isbn13[i];
        
        isbn10 += "" + checkdigit;
        return isbn10;
    }    
    
    private static String convertToISBN13(int[] isbn10) {
        String isbn13 = "";
        
        int sum = 0;
        for (int i = 0; i < isbn10.length; i++)
            sum += isbn10[i] * weight13[i];
        
        int remainder = sum % 10;
        
        int checkdigit = 0;
        if (remainder > 0)
            checkdigit = 10 - remainder;
        
        for (int i = 0; i < isbn10.length; i++)
            isbn13 += "" + isbn10[i];
        
        isbn13 += "" + checkdigit;
        return isbn13;
    }
}