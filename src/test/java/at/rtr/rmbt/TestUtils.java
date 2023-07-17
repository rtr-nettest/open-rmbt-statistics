package at.rtr.rmbt;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.experimental.UtilityClass;
import org.apache.poi.ss.usermodel.*;

import java.awt.image.BufferedImage;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

@UtilityClass
public class TestUtils {
    public static final ObjectMapper mapper = new ObjectMapper()
            .configure(JsonGenerator.Feature.IGNORE_UNKNOWN, true)
            .configure(JsonParser.Feature.IGNORE_UNDEFINED, true)
            .setSerializationInclusion(JsonInclude.Include.NON_NULL)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .registerModule(new JavaTimeModule());

    public static String asJsonString(final Object obj) throws JsonProcessingException {
        return mapper.writeValueAsString(obj);
    }

    public static String removeLineEndings(String source) {
        return source.replace("\n", "").replace("\r", "");
    }

    public static boolean compareImages(BufferedImage imgA, BufferedImage imgB) {
        // The images must be the same size.
        if (imgA.getWidth() != imgB.getWidth() || imgA.getHeight() != imgB.getHeight()) {
            return false;
        }

        int width = imgA.getWidth();
        int height = imgA.getHeight();

        // Loop over every pixel.
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                // Compare the pixels for equality.
                if (imgA.getRGB(x, y) != imgB.getRGB(x, y)) {
                    return false;
                }
            }
        }

        return true;
    }

    static void setFinalStatic(Field field, Object newValue) throws Exception {
        field.setAccessible(true);

        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);

        field.set(null, newValue);
    }

    public static void verifyDataInExcelBookAllSheets(Workbook workbook1, Workbook workbook2) {
        System.out.println("Verifying if both work books have same data.............");
        // Since we have already verified that both work books have same number of sheets so iteration can be done against any workbook sheet count
        int sheetCounts = workbook1.getNumberOfSheets();
        // So we will iterate through sheet by sheet
        for (int i = 0; i < sheetCounts; i++) {
            // Get sheet at same index of both work books
            Sheet s1 = workbook1.getSheetAt(i);
            Sheet s2 = workbook2.getSheetAt(i);
            System.out.println("*********** Sheet Name : " + s1.getSheetName() + "*************");
            // Iterating through each row
            int rowCounts = s1.getPhysicalNumberOfRows();
            for (int j = 0; j < rowCounts; j++) {
                // Iterating through each cell
                int cellCounts = s1.getRow(j).getPhysicalNumberOfCells();
                for (int k = 0; k < cellCounts; k++) {
                    // Getting individual cell
                    Cell c1 = s1.getRow(j).getCell(k);
                    Cell c2 = s2.getRow(j).getCell(k);
                    // Since cell have types and need o use different methods
                    if (c1.getCellType() == c2.getCellType()) {
                        if (c1.getCellType() == CellType.STRING) {
                            String v1 = c1.getStringCellValue();
                            String v2 = c2.getStringCellValue();
                            assertEquals(v1, v2, "Cell values are different.....");
                            System.out.println("Its matched : " + v1 + " === " + v2);
                        }
                        if (c1.getCellType() == CellType.NUMERIC) {
                            // If cell type is numeric, we need to check if data is of Date type
                            if (DateUtil.isCellDateFormatted(c1) | DateUtil.isCellDateFormatted(c2)) {
                                // Need to use DataFormatter to get data in given style otherwise it will come as time stamp
                                DataFormatter df = new DataFormatter();
                                String v1 = df.formatCellValue(c1);
                                String v2 = df.formatCellValue(c2);
                                assertEquals(v1, v2, "Cell values are different.....");
                                System.out.println("Its matched : " + v1 + " === " + v2);
                            } else {
                                double v1 = c1.getNumericCellValue();
                                double v2 = c2.getNumericCellValue();
                                assertEquals(v1, v2, "Cell values are different.....");
                                System.out.println("Its matched : " + v1 + " === " + v2);
                            }
                        }
                        if (c1.getCellType() == CellType.BOOLEAN) {
                            boolean v1 = c1.getBooleanCellValue();
                            boolean v2 = c2.getBooleanCellValue();
                            assertEquals(v1, v2, "Cell values are different.....");
                            System.out.println("Its matched : " + v1 + " === " + v2);
                        }
                    } else {
                        // If cell types are not same, exit comparison
                        fail("Non matching cell type.");
                    }
                }
            }
        }
        System.out.println("Hurray! Both work books have same data.");
    }
}
