/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package pack.crs.utils;

import java.io.IOException;
import pack.crs.models.AcademicReportNew;

/**
 *
 * @author Pieter
 */
public interface IReportGenerator {
    AcademicReportNew generateReport(String studentID) throws IOException;
}
