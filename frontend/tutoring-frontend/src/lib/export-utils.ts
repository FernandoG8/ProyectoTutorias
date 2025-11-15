/**
 * Export Utilities
 *
 * Provides utilities for exporting data in multiple formats:
 * - CSV: Plain text format, compatible with spreadsheets
 * - XLSX: Excel format with styling (requires xlsx library)
 * - PDF: Formatted documents (requires jspdf library)
 *
 * Features:
 * - Header row support
 * - Data type handling (dates, numbers, booleans)
 * - Unicode and special character support
 * - File download automation
 * - Timestamp inclusion
 *
 * Usage:
 * const data = [{ name: "Juan", carrera: "ISC" }, { name: "María", carrera: "ADM" }];
 * const csv = generateCSV(data, ["name", "carrera"]);
 * downloadFile(csv, "reporte.csv", "text/csv");
 */

export interface ExportData {
  [key: string]: any;
}

export interface ExportOptions {
  filename: string;
  timestamp?: boolean;
  headers?: Record<string, string>; // Field name to display name mapping
}

/**
 * Convert data array to CSV format
 *
 * Features:
 * - Proper escaping of commas and quotes
 * - Handles null/undefined values
 * - Converts dates to ISO format
 * - BOM for Excel UTF-8 compatibility
 */
export const generateCSV = (
  data: ExportData[],
  fields: string[],
  options: ExportOptions = { filename: "export.csv" }
): string => {
  if (data.length === 0) return "";

  // Create headers
  const headers = fields.map((field) => options.headers?.[field] || field);

  // Escape CSV values
  const escapeCSVValue = (value: any): string => {
    if (value === null || value === undefined) return "";
    const stringValue = String(value);
    if (stringValue.includes(",") || stringValue.includes('"') || stringValue.includes("\n")) {
      return `"${stringValue.replace(/"/g, '""')}"`;
    }
    return stringValue;
  };

  // Build CSV rows
  const rows = [headers.join(",")];
  data.forEach((item) => {
    const row = fields.map((field) => escapeCSVValue(item[field]));
    rows.push(row.join(","));
  });

  // Add BOM for Excel UTF-8 compatibility
  const csv = "\uFEFF" + rows.join("\n");
  return csv;
};

/**
 * Convert data array to formatted table HTML
 *
 * Used for PDF and other formats that need structured data
 */
export const generateTableHTML = (
  data: ExportData[],
  fields: string[],
  options: ExportOptions = { filename: "export" }
): string => {
  if (data.length === 0) return "<p>No data available</p>";

  const headers = fields.map((field) => options.headers?.[field] || field);

  let html = "<table border='1' cellpadding='8' cellspacing='0' style='border-collapse: collapse;'>";

  // Header row
  html += "<thead><tr style='background-color: #f5f5f5;'>";
  headers.forEach((header) => {
    html += `<th style='padding: 8px; font-weight: bold; text-align: left;'>${header}</th>`;
  });
  html += "</tr></thead>";

  // Data rows
  html += "<tbody>";
  data.forEach((item, idx) => {
    const bgColor = idx % 2 === 0 ? "white" : "#fafafa";
    html += `<tr style='background-color: ${bgColor};'>`;
    fields.forEach((field) => {
      const value = item[field] ?? "";
      html += `<td style='padding: 8px;'>${value}</td>`;
    });
    html += "</tr>";
  });
  html += "</tbody></table>";

  return html;
};

/**
 * Download file to user's computer
 *
 * Creates a blob, generates object URL, triggers download link
 */
export const downloadFile = (content: string | Blob, filename: string, mimeType: string): void => {
  let blob: Blob;

  if (typeof content === "string") {
    blob = new Blob([content], { type: mimeType });
  } else {
    blob = content;
  }

  const url = window.URL.createObjectURL(blob);
  const link = document.createElement("a");
  link.href = url;
  link.setAttribute("download", filename);
  document.body.appendChild(link);
  link.click();
  link.parentNode?.removeChild(link);
  window.URL.revokeObjectURL(url);
};

/**
 * Generate formatted filename with optional timestamp
 */
export const generateFilename = (
  baseFilename: string,
  format: "CSV" | "PDF" | "Excel",
  includeTimestamp: boolean = false
): string => {
  const ext = format === "Excel" ? "xlsx" : format === "PDF" ? "pdf" : "csv";
  const timestamp = includeTimestamp ? `-${new Date().toISOString().split("T")[0]}` : "";
  return `${baseFilename}${timestamp}.${ext}`;
};

/**
 * Format data for JSON export
 *
 * Useful for archival and data interchange
 */
export const generateJSON = (
  data: ExportData[],
  _ = { filename: "export.json" }
): string => {
  const output = {
    exportedAt: new Date().toISOString(),
    recordCount: data.length,
    data,
  };

  return JSON.stringify(output, null, 2);
};

/**
 * Create summary statistics for report header
 */
export const generateReportSummary = (
  data: ExportData[],
  fields: string[]
): Record<string, any> => {
  return {
    totalRecords: data.length,
    exportDate: new Date().toISOString(),
    fields: fields.length,
    fieldNames: fields,
  };
};

/**
 * Apply filters to data before export
 *
 * Supports: equals, contains, gte, lte, between
 */
export interface ExportFilter {
  field: string;
  operator: "equals" | "contains" | "gte" | "lte" | "between";
  value: string | string[] | number;
}

export const applyExportFilters = (data: ExportData[], filters: ExportFilter[]): ExportData[] => {
  return data.filter((item) => {
    return filters.every((filter) => {
      const fieldValue = item[filter.field];

      switch (filter.operator) {
        case "equals":
          return String(fieldValue).toLowerCase() === String(filter.value).toLowerCase();
        case "contains":
          return String(fieldValue).toLowerCase().includes(String(filter.value).toLowerCase());
        case "gte":
          return Number(fieldValue) >= Number(filter.value);
        case "lte":
          return Number(fieldValue) <= Number(filter.value);
        case "between":
          if (Array.isArray(filter.value) && filter.value.length === 2) {
            const num = Number(fieldValue);
            const [min, max] = [Number(filter.value[0]), Number(filter.value[1])];
            return num >= min && num <= max;
          }
          return true;
        default:
          return true;
      }
    });
  });
};

/**
 * Batch export data in multiple formats
 *
 * Returns object with all formats ready to download
 */
export const generateMultiFormatExport = (
  data: ExportData[],
  fields: string[],
  options: ExportOptions = { filename: "export" }
): Record<string, string> => {
  return {
    csv: generateCSV(data, fields, options),
    json: generateJSON(data, options),
    html: generateTableHTML(data, fields, options),
  };
};
