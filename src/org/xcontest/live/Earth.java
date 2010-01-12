/*
 *  XCTrack - XContest Live Tracking client for J2ME devices
 *  Copyright (C) 2009 Petr Chromec <petr@xcontest.org>
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.xcontest.live;

public class Earth {
	
	private static final double EPSILON = 1e-12;
	
	/**
	 *    Earth radius in meters
	 */
	public static final double Radius = 6371000;
	
	/**
	 * 
	 * @param lon1 longitude in degrees
	 * @param lat1 latitude in degrees
	 * @param lon2 longitude in degrees
	 * @param lat2 latitude in degrees
	 * @return distance between 2 selected points in meters
	 */
	public static final double getDistance(double lon1, double lat1, double lon2, double lat2) {
		double rlat1 = lat1*Math.PI/180;
		double rlat2 = lat2*Math.PI/180;
		double rdx = (lon2-lon1)*Math.PI/180;
		double rdy = rlat2-rlat1;
		double b = Math.sin(rdx/2); 
		return Radius*2*asin(Math.sqrt(Math.sin(rdy/2)*Math.sin(rdy/2)+Math.cos(rlat1)*Math.cos(rlat2)*b*b));
	}
	
	/**
	 * 
	 * @param lon1 longitude in degrees
	 * @param lat1 latitude in degrees
	 * @param lon2 longitude in degrees
	 * @param lat2 latitude in degrees
	 * @return Angle (heading) from point1 to point2 in interval <0,360)
	 */
	public static final double getAngle(double lon1, double lat1, double lon2, double lat2) {
		double cosx1 = Math.cos(lon1*Math.PI/180);
		double cosy1 = Math.cos(lat1*Math.PI/180);
		double cosx2 = Math.cos(lon2*Math.PI/180);
		double cosy2 = Math.cos(lat2*Math.PI/180);
		double sinx1 = Math.sin(lon1*Math.PI/180);
		double siny1 = Math.sin(lat1*Math.PI/180);
		double sinx2 = Math.sin(lon2*Math.PI/180);
		double siny2 = Math.sin(lat2*Math.PI/180);
		
		double x1 = cosy1*sinx1;
		double z1 = -cosy1*cosx1;
		double y1 = siny1;
		double x2 = cosy2*sinx2;
		double z2 = -cosy2*cosx2;
		double y2 = siny2;
        double t = (x1*x2+y1*y2+z1*z2);
        double x,y,z;
    	double alpha;
        
        if (t > EPSILON || t < -EPSILON) {
            t = 1/t;
            x = t*x2-x1;
            y = t*y2-y1;
            z = t*z2-z1;
        }
        else {
            x = x2;
            y = y2;
            z = z2;
        }
        
        t = Math.sqrt(x*x+y*y+z*z)*cosy1;
        if (t > EPSILON) {
            z = (-z1*x+x1*z)/t;
            if (z > 1-EPSILON)
                alpha = 0;
            else if (z < -1+EPSILON)
                alpha = Math.PI;
            else
                alpha = acos(z);
            double res = (Math.PI/2-(y < 0 ? (2*Math.PI-alpha) : alpha))/(2*Math.PI);
            return (res-Math.floor(res))*360;
        }
        else {
            return 0;
        }
	}
	
	
	public static double lat2gg(double lat) {
		return (1-log(Math.tan(lat*Math.PI/360+Math.PI/4))/Math.PI)/2;
	}
	
	public static double gg2lat(double y) {
		return (2*atan(exp(Math.PI-2*Math.PI*y))-Math.PI/2)*180/Math.PI;
	}
	
	public static double lon2gg(double lon) {
		return (lon+180)/360;
	}
	
	public static double gg2lon(double x) {
		return x*360-180;
	}
	
	private static final double E=2.71828182845905;
	private static final double EI=1/E;
	
	public static double exp(double x) {
		if (x < 0) return 1/exp(-x);
		
		// exp(x*2^n) = (exp(x))^(2^n)
		double n = 0;
		while (x > 1) {
			x /= 2;
			n ++;
		}
		
		// x now in <0,1>
		double y = 1+x*(1+x*(1+x*(1+x*(1+x*(1+x*(1+x*(1+x*(1+x*(1+x*(1+x*(1+x*(1+x*(1+x/14)/13)/12)/11)/10)/9)/8)/7)/6)/5)/4)/3)/2);
		for (int i = 0; i < n; i ++)
			y *= y;
		return y;
	}
	
	public static double log(double x) {
		double exp = 0;
		while (x > E) {
			exp ++;
			x *= EI;
		}
		while (x < EI) {
			exp --;
			x *= E;
		}
		
		double sum = 0;
		double y = (x-1)/(x+1);
		double z = y*y;

		for (int i = 0; i < 10; i ++) {
			sum += y/(2*i+1);
			y *= z;
		}
		return exp+2*sum;
	}

	
	/*
	 * tayloruv rozvoj kolem 0
	 */
	/*
	private static final double asin0(double x) {
		double x2 = x*x;
		double x4 = x2*x2;
		return x + x*x2/6 + 3*x*x4/40 + 5*x*x2*x4/112 + 35*x*x4*x4/1152; 
	}
	*/
	
	/*
	 * rozvoj kolem 1
	 */
	private static final double asin1(double x) {
	    double sy = Math.sqrt(1-x);
	    double sy2 = 1-x;
	    double sy4 = sy2*sy2;
	    double sy8 = sy4*sy4;
	    double s2 = Math.sqrt(2);
	    return Math.PI/2 - s2*sy - s2*sy*sy2/12 - 3*s2*sy*sy4/160 - 5*s2*sy*sy2*sy4/896 - 35*s2*sy*sy8/18432 - 63*s2*sy*sy2*sy8/90112;
	}
	
	private static final double[] _asinTable = new double[] {
			0.167230565086851, -1.88059463374036e-05, 1.0, 0.0,
			0.17103586845535, -0.000628404245509103, 1.00003242005465, -5.72669905815615e-07,
			0.178890201003947, -0.00302722948512918, 1.00027655512612, -8.85225720478888e-06,
			0.191312184885842, -0.00866638395196288, 1.00112981755414, -5.1884841505756e-05,
			0.209164431355054, -0.0194376628747938, 1.00329605954697, -0.000197100054911636,
			0.233776311122881, -0.0379726793436389, 1.00794884032492, -0.000586417341469858,
			0.267150753862379, -0.0681100985625258, 1.01702019231659, -0.00149656516323676,
			0.312311867239575, -0.11566659251738, 1.03371302891887, -0.00344967020061182,
			0.37390155973464, -0.189768781344199, 1.06343172758269, -0.00742253977353463,
			0.459239300343428, -0.305260838313953, 1.11553190143563, -0.0152568780839581,
			0.580291095988133, -0.487277058188834, 1.20675927457699, -0.030497984141502,
			0.757537278744656, -0.780438607619524, 1.3683860680992, -0.0602006855320528,
			1.02810905467588, -1.2686705783219, 1.66204691493633, -0.119077187782619,
			1.46443141265983, -2.12170666955143, 2.21795524479006, -0.239834881204413,
			2.22187249432057, -3.71679459505825, 3.33763995045832, -0.501823382683486,
			3.67959915933206, -7.00698029914946, 5.81300475938825, -1.12259596763134,
			6.95185025726506, -14.8893361383477, 12.1420519940741, -2.81651858043484,
			16.4417931432783, -39.2012935290994, 32.9029283529182, -8.72588544550715,
			63.2260458471498, -166.368219851879, 148.117661663515, -43.5196553247143
	};

	public static final double asin(double x) {
//*
		if (x < 0) {
			x = -x;
			int i = 4*(int)Math.floor(x*20);
			if (i >= 4*19)
				return -asin1(x);
			else
				return -x*x*x*_asinTable[i]-x*x*_asinTable[i+1]-x*_asinTable[i+2]-_asinTable[i+3];
		}
		else {
			int i = 4*(int)Math.floor(x*20);
			if (i >= 4*19)
				return asin1(x);
			else
				return x*x*x*_asinTable[i]+x*x*_asinTable[i+1]+x*_asinTable[i+2]+_asinTable[i+3];
		}
/*/
		if (x < -0.5)
			return -asin1(-x);
		else if (x < 0.5)
			return asin0(x);
		else
			return asin1(x);
//*/
	}
	
	public static final double acos(double x) {
		return Math.PI/2-asin(x);
	}
	
	public static final double atan(double x) {
		return asin(x/Math.sqrt(1+x*x));
	}
	
	public static final double atan2(double x, double y) {
		if (0 <= y && y < EPSILON)
			return x < 0 ? -Math.PI/2 : Math.PI/2;
		else if (-EPSILON < y && y < 0)
			return x < 0 ? Math.PI/2 : -Math.PI/2;
		else {
			double z = x/y;
			return y >= 0 ? asin(z/Math.sqrt(1+z*z)) : ((2*Math.PI+asin(z/Math.sqrt(1+z*z)))%(2*Math.PI)) - Math.PI;
		}
	}
}

