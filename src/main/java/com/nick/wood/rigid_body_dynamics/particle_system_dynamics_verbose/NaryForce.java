package com.nick.wood.rigid_body_dynamics.particle_system_dynamics_verbose;

import com.nick.wood.rigid_body_dynamics.graphics.math.Vec3d;

public interface NaryForce {
	Vec3d calculateForceOnParticle(Particle ... particle);
}