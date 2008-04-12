/********************************************************************
    Copyright (c) 2000-2008 Steven E. Hugg.

    This file is part of Exoflight.

    Exoflight is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Exoflight is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Exoflight.  If not, see <http://www.gnu.org/licenses/>.
*********************************************************************/
package com.fasterlight.exo.orbit.traj.test;

import java.io.*;

import junit.framework.*;

import com.fasterlight.exo.game.SpaceGame;
import com.fasterlight.exo.orbit.*;
import com.fasterlight.exo.orbit.traj.*;
import com.fasterlight.exo.ship.*;
import com.fasterlight.exo.strategy.Vehicle;
import com.fasterlight.testing.*;
import com.fasterlight.vecmath.Vector3d;

public class MutableTrajectoryTests
extends NumericTestCase
{
	RandomnessProvider rnd = new RandomnessProvider();
	int NUM_ITERS = 5000;

	//

	public MutableTrajectoryTests(String name)
	{
		super(name);
		THRESHOLD = 1e-5;
	}

	public Orientation getRandomOrt()
	{
		Vector3d a = rnd.rndvec();
		Vector3d b = rnd.rndvec();
		return new Orientation(a,b);
	}

	//

	public void testCowell1() throws FileNotFoundException
	{
		SpaceGame game = new SpaceGame();
		game.start();

		Planet planet = (Planet)game.getBody("Earth");
		
		CowellTrajectory traj = new CowellTrajectory();
		traj.setParent(planet);
		double alt = 3;
		Vector3d r = new Vector3d(0, 0, planet.getRadius()+alt);
		Vector3d v = new Vector3d(planet.getAirVelocity(r, game.time() * (1d/Constants.TICKS_PER_SEC)));
		traj.setStateVector(new StateVector(r, v));

		DefaultMutableTrajectory.perturbFlags = DefaultMutableTrajectory.PF_DRAG;
  		Vehicle vehicle = Vehicle.getVehicle("Space Wagon");
  		Structure struct = vehicle.toStructure(game.getAgency());
		SpaceShip ship = new SpaceShip(struct);
		ship.setTrajectory(traj);
		//ship.getShipAttitudeSystem().setThrottleManual(1);
		CowellTrajectory.debug = true;
		//CowellTrajectory.debug2 = true;
		CowellTrajectory.debug3 = true;
		long tlast = 0;
		long t0 = game.time();
		PrintStream out = new PrintStream(new FileOutputStream("trv.csv"));
		for (int i=0; i<100000; i++)
		{
			game.update(1);
			long t = traj.getLastT0(); 
			if (tlast != t)
			{
				StateVector lastInitialState = traj.getLastInitialState();
				//System.out.println("--->" + (t-t0) + ": " + lastInitialState);
				out.print(t-t0);
				out.print(',');
				out.print(lastInitialState.r.x);
				out.print(',');
				out.print(lastInitialState.r.y);
				out.print(',');
				out.print(lastInitialState.r.z);
				out.print(',');
				out.print(lastInitialState.v.x);
				out.print(',');
				out.print(lastInitialState.v.y);
				out.print(',');
				out.print(lastInitialState.v.z);
				out.println();
				tlast = t;
				assertTrue(!Double.isNaN(lastInitialState.r.length()));
			}
			//ship.getShipAttitudeSystem().setManualThrottle(0);
		}
		out.println(traj);
	}
	
	public void testMutable1()
	{
		SpaceGame game = new SpaceGame();
		game.start();

		for (int i=0; i<NUM_ITERS; i++)
		{
			Planet planet = (Planet)game.getBody("Earth");

			CowellTrajectory traj = new CowellTrajectory();
			traj.setParent(planet);

			Vector3d r = rnd.rndvec();
			Vector3d v = rnd.rndvec();
			traj.setStateVector(new StateVector(r, v));

			Orientation o1 = getRandomOrt();
			traj.setVelRefOrientation(o1);
			Orientation o2 = traj.getVelRefOrientation();

			compareAssert(o1.getDirection(), o2.getDirection());
		}
	}

	//

	public static Test suite()
	{
		TestSuite suite = new TestSuite(MutableTrajectoryTests.class);
		return suite;
	}

}
