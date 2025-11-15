# Week 4 Advanced Features - Quick Reference Guide

## Components at a Glance

### 1. ReportBuilder
**Import**: `import { ReportBuilder } from "@/components/common/ReportBuilder"`

**Basic Usage**:
```typescript
<ReportBuilder
  title="Generar Reporte"
  availableFields={[
    { name: "carrera", label: "Carrera", type: "select" },
  ]}
  onGenerate={async (config) => {
    console.log("Generated:", config);
  }}
  onExport={async (config) => {
    const blob = await exportReport(config);
    return blob;
  }}
/>
```

**Props**:
- `title` (string) - Form title
- `onGenerate` (function) - Callback after generation
- `onExport` (function) - Export callback returning Blob
- `availableFields` (array) - List of filterable fields

---

### 2. AnalyticsDashboard
**Import**: `import { AnalyticsDashboard } from "@/components/common/AnalyticsDashboard"`

**Basic Usage**:
```typescript
<AnalyticsDashboard
  title="Analytics"
  metrics={[
    { label: "Total", value: 100, color: "success" },
  ]}
  trendData={[
    { date: "Jan", value: 50 },
    { date: "Feb", value: 75 },
  ]}
  distributionData={[
    { name: "Category A", value: 60 },
    { name: "Category B", value: 40 },
  ]}
  performanceData={[
    { category: "Q1", metric1: 100, metric2: 80 },
  ]}
/>
```

**Props**:
- `title` (string)
- `metrics` (array) - KPI cards
- `trendData` (array) - For line/area chart
- `distributionData` (array) - For pie chart
- `performanceData` (array) - For bar chart

---

### 3. BulkOperations
**Import**: `import { BulkOperations } from "@/components/common/BulkOperations"`

**Basic Usage**:
```typescript
<BulkOperations
  title="Bulk Update"
  items={students.map(s => ({
    id: s.id,
    label: s.nombre,
    description: s.matricula,
  }))}
  actionLabel="Update"
  confirmationTitle="Confirm?"
  confirmationMessage="Update these items?"
  onExecute={async (ids) => {
    await updateMany(ids);
  }}
/>
```

**Props**:
- `title`, `description` (string)
- `items` (array) - Items to operate on
- `actionLabel`, `confirmationTitle`, `confirmationMessage` (string)
- `onExecute` (function) - Main action callback
- `onRetry` (function) - Retry failed items
- `variant` - "default" | "destructive" | "success"

---

### 4. StudentPerformanceTracker
**Import**: `import { StudentPerformanceTracker } from "@/components/common/StudentPerformanceTracker"`

**Basic Usage**:
```typescript
<StudentPerformanceTracker
  studentName="Juan Pérez"
  currentGPA={3.45}
  targetGPA={3.0}
  cohortAverageGPA={3.2}
  isAtRisk={false}
  trendData={[
    { semester: "2024-1", gpa: 3.2 },
    { semester: "2024-2", gpa: 3.45 },
  ]}
  courses={courses}
/>
```

**Props**:
- `studentName` (string)
- `currentGPA`, `targetGPA`, `cohortAverageGPA` (number)
- `isAtRisk` (boolean)
- `riskFactors` (array of strings)
- `trendData` (array) - GPA history
- `courses` (array) - Course records
- `metrics` (array) - Additional metrics

---

### 5. TutorWorkloadOptimization
**Import**: `import { TutorWorkloadOptimization } from "@/components/common/TutorWorkloadOptimization"`

**Basic Usage**:
```typescript
<TutorWorkloadOptimization
  tutors={tutorList}
  recommendations={recommendations}
  targetUtilization={0.85}
  onOptimize={async () => {
    await optimizeWorkload();
  }}
  onRebalance={async () => {
    await rebalanceWorkload();
  }}
/>
```

**Props**:
- `tutors` (array) - TutorWorkload objects
- `recommendations` (array) - OptimizationRecommendation objects
- `targetUtilization` (number) - 0-1 (default 0.85)
- `onOptimize`, `onRebalance` (functions)

---

### 6. AuditLogViewer
**Import**: `import { AuditLogViewer } from "@/components/common/AuditLogViewer"`

**Basic Usage**:
```typescript
<AuditLogViewer
  logs={auditLogs}
  pageSize={25}
  onExport={async (logs) => {
    const csv = generateCSV(logs, ["timestamp", "user.name", "action"]);
    downloadFile(csv, "audit.csv", "text/csv");
  }}
/>
```

**Props**:
- `logs` (array) - AuditLogEntry objects
- `isLoading` (boolean)
- `pageSize` (number) - default 25
- `onExport` (function) - Export callback

---

## Utility Functions

### export-utils.ts Functions

```typescript
import {
  generateCSV,
  generateJSON,
  generateTableHTML,
  downloadFile,
  generateFilename,
  applyExportFilters,
  generateReportSummary,
} from "@/lib/export-utils";

// Generate CSV
const csv = generateCSV(
  data,                    // Array of objects
  ["field1", "field2"],   // Fields to include
  {
    filename: "export",
    headers: { field1: "Field 1 Label" },
  }
);

// Download
downloadFile(csv, "export.csv", "text/csv");

// Apply filters
const filtered = applyExportFilters(data, [
  { field: "status", operator: "equals", value: "ACTIVE" },
  { field: "score", operator: "gte", value: 80 },
]);

// Generate filename with timestamp
const filename = generateFilename("report", "CSV", true);
// Result: "report-2024-11-14.csv"
```

---

## Color Variants

### Component Variants

```typescript
// Badge variants
<Badge variant="success">Completado</Badge>
<Badge variant="warning">Pendiente</Badge>
<Badge variant="danger">Error</Badge>
<Badge variant="info">Información</Badge>

// Button variants
<Button variant="primary">Primary</Button>
<Button variant="secondary">Secondary</Button>
<Button variant="ghost">Ghost</Button>
<Button variant="destructive">Delete</Button>

// Chart colors
colors.primary[400]     // Charts, primary elements
colors.success[400]     // Success, positive
colors.warning[400]     // Warning, caution
colors.danger[400]      // Error, danger
colors.info[400]        // Info, secondary
```

---

## Common Integration Patterns

### Pattern 1: Report Generation with Backend

```typescript
const { data: reportData, isLoading } = useQuery({
  queryKey: ["report"],
  queryFn: () => fetchReportData(),
});

<ReportBuilder
  isLoading={isLoading}
  availableFields={reportFields}
  onExport={async (config) => {
    const response = await fetch("/api/reports/export", {
      method: "POST",
      body: JSON.stringify(config),
    });
    return await response.blob();
  }}
/>
```

### Pattern 2: Bulk Operations with Mutation

```typescript
const mutation = useMutation({
  mutationFn: (ids: number[]) => updateMany(ids),
  onSuccess: () => {
    queryClient.invalidateQueries({ queryKey: ["items"] });
    addNotification("success", "Updated successfully");
  },
});

<BulkOperations
  items={items}
  onExecute={async (ids) => {
    await mutation.mutateAsync(ids);
  }}
/>
```

### Pattern 3: Performance Tracking with Data Transformation

```typescript
const { data: studentData } = useQuery({
  queryKey: ["student", studentId],
  queryFn: () => fetchStudent(studentId),
});

const performanceProps = useMemo(() => ({
  studentName: studentData?.nombre,
  currentGPA: studentData?.gpa,
  courses: studentData?.cursos || [],
  trendData: calculateTrends(studentData?.history),
}), [studentData]);

<StudentPerformanceTracker {...performanceProps} />
```

### Pattern 4: Analytics with Multiple Queries

```typescript
const { data: metrics } = useQuery(...);
const { data: trends } = useQuery(...);
const { data: distribution } = useQuery(...);

<AnalyticsDashboard
  metrics={formatMetrics(metrics)}
  trendData={trends}
  distributionData={distribution}
/>
```

---

## Type Quick Reference

### ReportFilter
```typescript
interface ReportFilter {
  field: string;
  operator: "equals" | "contains" | "gte" | "lte" | "between";
  value: string | string[];
}
```

### TutorWorkload
```typescript
interface TutorWorkload {
  tutorId: number;
  tutorName: string;
  currentLoad: number;
  capacity: number;
  performance?: number;
}
```

### AuditLogEntry
```typescript
interface AuditLogEntry {
  id: number;
  timestamp: string;
  user: { id: number; name: string; email: string };
  action: "CREATE" | "UPDATE" | "DELETE" | "VIEW" | "EXPORT";
  entityType: string;
  severity: "info" | "warning" | "critical";
  status: "success" | "failure";
}
```

---

## Styling Cheat Sheet

```typescript
// Text colors
color: colors.semantic.text.primary      // Headers
color: colors.semantic.text.secondary    // Descriptions
color: colors.semantic.text.muted        // Helper text

// Backgrounds
backgroundColor: colors.primary[50]      // Light backgrounds
backgroundColor: colors.semantic.background

// Borders
borderColor: colors.semantic.border
borderColor: colors.danger[200]          // Error borders

// Action colors
backgroundColor: colors.success[400]     // Success button
backgroundColor: colors.danger[400]      // Danger button
```

---

## Common Recipes

### Recipe 1: Export Filtered Data

```typescript
const handleExport = async (filters: ReportFilter[]) => {
  const data = await fetchData();
  const filtered = applyExportFilters(data, filters);
  const csv = generateCSV(filtered, ["field1", "field2"]);
  const filename = generateFilename("report", "CSV", true);
  downloadFile(csv, filename, "text/csv");
};
```

### Recipe 2: Status Badge Display

```typescript
const statusColor = {
  "ACTIVO": "success",
  "INACTIVO": "warning",
  "PENDIENTE": "info",
  "ERROR": "danger",
}[status];

<Badge variant={statusColor}>{status}</Badge>
```

### Recipe 3: Form with Validation

```typescript
const { register, handleSubmit, formState: { errors } } = useForm();

<FormField
  label="Email"
  error={errors.email}
  helperText="Enter a valid email"
  {...register("email", { required: true })}
/>
```

### Recipe 4: Paginated List

```typescript
const [page, setPage] = useState(1);
const pageSize = 20;
const start = (page - 1) * pageSize;
const items = allItems.slice(start, start + pageSize);
const totalPages = Math.ceil(allItems.length / pageSize);
```

---

## Troubleshooting

### Charts Not Rendering
```typescript
// Ensure data has required fields
trendData should have: { date: string, value: number }
distributionData should have: { name: string, value: number }

// Check ResponsiveContainer parent has height
<div className="h-80">
  <ResponsiveContainer>...
</div>
```

### Colors Not Applied
```typescript
// Use inline styles, not classes
style={{ backgroundColor: colors.primary[400] }}

// Not:
className="bg-primary-400"  // ❌ This won't work
```

### Filters Not Working
```typescript
// Ensure filter operators match data types
"equals" → exact match
"contains" → substring match
"gte" / "lte" → number comparison
"between" → requires array of 2 numbers
```

---

## Performance Tips

1. **Pagination**: Use pageSize of 20-25 for tables
2. **Memoization**: Wrap chart data transforms in useMemo
3. **Lazy Load**: Only render charts when data available
4. **Debounce**: Search at 300ms intervals
5. **Pagination**: Split large lists into pages

---

## Next Steps

1. **Integrate components** into your pages
2. **Connect to backend** via services
3. **Add unit tests** for component behavior
4. **Configure styling** for brand colors
5. **Deploy** to production

---

**Happy building! 🚀**
