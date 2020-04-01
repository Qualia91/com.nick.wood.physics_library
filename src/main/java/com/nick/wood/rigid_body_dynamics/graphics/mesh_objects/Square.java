package com.nick.wood.rigid_body_dynamics.graphics.mesh_objects;

import com.nick.wood.rigid_body_dynamics.graphics.Mesh;
import com.nick.wood.rigid_body_dynamics.graphics.Vertex;
import com.nick.wood.rigid_body_dynamics.maths.Matrix4d;
import com.nick.wood.rigid_body_dynamics.maths.Vec3d;

public class Square implements MeshObject{

	private Vec3d position, scale;
	private Matrix4d rotation;

	private final Mesh mesh = new Mesh(new Vertex[] {
			new Vertex(new Vec3d(-0.5,  0.5, 0.0), new Vec3d(1.0, 0.0, 0.0)),
			new Vertex(new Vec3d( 0.5,  0.5, 0.0), new Vec3d(0.0, 1.0, 0.0)),
			new Vertex(new Vec3d( 0.5, -0.5, 0.0), new Vec3d(0.0, 0.0, 1.0)),
			new Vertex(new Vec3d(-0.5, -0.5, 0.0), new Vec3d(1.0, 1.0, 0.0))
	}, new int[] {
			0, 1, 2,
			0, 3, 2
	});

	public Square() {
	}

	public Mesh getMesh() {
		return mesh;
	}

	@Override
	public Matrix4d getTransformation() {
		return Matrix4d.Identity;
	}
}