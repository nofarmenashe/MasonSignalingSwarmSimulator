package sim.app.signalingswarmgame;

import sim.util.Double2D;

import java.util.Arrays;
import java.util.Vector;

public class ConvexHull {

    public static double cross(Double2D O, Double2D A, Double2D B) {
        return (A.x - O.x) * (double) (B.y - O.y) - (A.y - O.y) * (double) (B.x - O.x);
    }

    public static Double2D[] coverPolygon(Double2D[] points, int n) {

        // Initialize Result
        Vector<Double2D> hull = new Vector<Double2D>();

        // Find the leftmost point
        int l = 0;
        for (int i = 1; i < n; i++)
            if (points[i].x < points[l].x)
                l = i;

        // Start from leftmost point, keep moving
        // counterclockwise until reach the start point
        // again. This loop runs O(h) times where h is
        // number of points in result or output.
        int p = l, q;
        do
        {
            // Add current point to result
            hull.add(points[p]);

            // Search for a point 'q' such that
            // orientation(p, x, q) is counterclockwise
            // for all points 'x'. The idea is to keep
            // track of last visited most counterclock-
            // wise Double2D in q. If any point 'i' is more
            // counterclock-wise than q, then update q.
            q = (p + 1) % n;

            for (int i = 0; i < n; i++)
            {
                // If i is more counterclockwise than
                // current q, then update q
                if (orientation(points[p], points[i], points[q])
                        == 2)
                    q = i;
            }

            // Now q is the most counterclockwise with
            // respect to p. Set p as q for next iteration,
            // so that q is added to result 'hull'
            p = q;

        } while (p != l);  // While we don't come to first
        // point

        // Print Result
//        for (Double2D temp : hull)
//            System.out.println("(" + temp.x + ", " +
//                    temp.y + ")");
        Double2D[] hullArray = new Double2D[hull.size()];
        hull.toArray(hullArray);
        return hullArray;
    }

    public static int orientation(Double2D p, Double2D q, Double2D r)
    {
        double val = (q.y - p.y) * (r.x - q.x) -
                (q.x - p.x) * (r.y - q.y);

        if (val == 0) return 0;  // collinear
        return (val > 0)? 1: 2; // clock or counterclock wise
    }

    public static double polygonArea(Double2D[] convexHull){
        double sum = 0;
        for (int i = 0; i < convexHull.length ; i++)
        {
//            System.out.println(convexHull[i].x +", " + convexHull[i].y);
            if (i == 0)
                sum += convexHull[i].x * (convexHull[i + 1].y - convexHull[convexHull.length - 1].y);
            else if (i == convexHull.length - 1)
                sum += convexHull[i].x * (convexHull[0].y - convexHull[i - 1].y);
            else
                sum += convexHull[i].x * (convexHull[i + 1].y - convexHull[i - 1].y);
        }

        double area = 0.5 * Math.abs(sum);
        return area;
    }
}