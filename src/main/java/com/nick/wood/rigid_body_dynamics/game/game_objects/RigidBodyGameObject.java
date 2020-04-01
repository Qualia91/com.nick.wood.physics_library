package com.nick.wood.rigid_body_dynamics.game.game_objects;

import com.nick.wood.rigid_body_dynamics.graphics.mesh_objects.MeshGroup;
import com.nick.wood.rigid_body_dynamics.maths.Matrix4d;
import com.nick.wood.rigid_body_dynamics.maths.Vec3d;

public class RigidBodyGameObject implements GameObject {

	private Vec3d position, scale;
	private Matrix4d rotation;
	private final MeshGroup meshGroup;

	public RigidBodyGameObject(Vec3d position, Matrix4d rotation, Vec3d scale, MeshGroup meshGroup) {
		this.position = position;
		this.rotation = rotation;
		this.scale = scale;
		this.meshGroup = meshGroup;
	}

	public void update() {
		//position = position.add(new Vec3d(0.0, 0.0, -0.01));
		//scale = scale.add(Vec3d.Y.scale(0.0));
		//rotation = rotation.add(Vec3d.Y.scale(1));
	}

	public Vec3d getPosition() {
		return position;
	}

	public void setPosition(Vec3d position) {
		this.position = position;
	}

	public Matrix4d getRotation() {
		return rotation;
	}

	public void setRotation(Matrix4d rotation) {
		this.rotation = rotation;
	}

	public Vec3d getScale() {
		return scale;
	}

	public void setScale(Vec3d scale) {
		this.scale = scale;
	}

	public MeshGroup getMeshGroup() {
		return meshGroup;
	}

	@Override
	public void rotateLeft() {

	}

	@Override
	public void rotateRight() {

	}
}