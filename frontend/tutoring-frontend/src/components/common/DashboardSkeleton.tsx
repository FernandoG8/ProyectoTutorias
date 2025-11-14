import { Card } from "@/components/ui/Card";
import { CardSkeleton } from "./CardSkeleton";

export function DashboardSkeleton() {
  return (
    <div className="space-y-6">
      <section className="grid gap-4 md:grid-cols-4">
        {Array.from({ length: 4 }).map((_, i) => (
          <Card key={i}>
            <CardSkeleton />
          </Card>
        ))}
      </section>
      <section className="grid gap-6 lg:grid-cols-5">
        <Card className="lg:col-span-3">
          <div className="h-80">
            <CardSkeleton />
          </div>
        </Card>
        <Card className="lg:col-span-2">
          <CardSkeleton />
        </Card>
      </section>
    </div>
  );
}

