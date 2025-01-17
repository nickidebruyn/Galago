/**
 * Copyright (c) 2014, jMonkeyEngine All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * Neither the name of 'jMonkeyEngine' nor the names of its contributors may be
 * used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package com.jme3.ai.agents.behaviors.npc.steering;

import com.jme3.ai.agents.Agent;
import com.jme3.ai.agents.AgentExceptions;
import com.jme3.ai.agents.Team;
import com.jme3.ai.agents.util.GameEntity;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;
import java.util.List;

/**
 * Craig W. Reynolds: "Alignment steering behavior gives an character the
 * ability to align itself with (that is, head in the same direction as) other
 * nearby characters. Steering for alignment can be computed by finding all
 * characters in the local neighborhood, averaging the unit forward vector of
 * the nearby characters. This average is the “desired velocity,” and so the
 * steering vector is the difference between the average and our character’s
 * forward vector. This steering will tend to turn our character so it is
 * aligned with its neighbors."
 *
 * @author Jesús Martín Berlanga
 * @version 1.2.1
 */
public class AlignmentBehavior extends AbstractStrengthSteeringBehavior {

    /**
     * List of game entities that should align.
     */
    private List<GameEntity> neighbours;
    /**
     * Maximum distance between two agents.
     */
    private float maxDistance = Float.POSITIVE_INFINITY;
    private float maxAngle = FastMath.PI / 2;

    /**
     * maxAngle is setted to PI / 2 by default and maxDistance to infinite.
     * Neighbours of agent will be his team members.
     *
     * @param agent
     */
    public AlignmentBehavior(Agent agent) {
        super(agent);
        try {
            neighbours = convertToGameEntities(agent.getTeam().getMembers());
        } catch (NullPointerException npe) {
            throw new AgentExceptions.TeamNotFoundException(agent);
        }
    }

    /**
     * Neighbours of agent will be his team members.
     *
     * @param maxDistance In order to consider a neighbour inside the
     * neighbourhood
     * @param maxAngle In order to consider a neighbour inside the neighbourhood
     * @param agent
     */
    public AlignmentBehavior(Agent agent, float maxDistance, float maxAngle) {
        super(agent);
        try {
            this.validateMaxDistance(maxDistance);
            this.maxDistance = maxDistance;
            this.maxAngle = maxAngle;
            neighbours = convertToGameEntities(agent.getTeam().getMembers());
        } catch (NullPointerException npe) {
            throw new AgentExceptions.TeamNotFoundException(agent);
        }
    }

    /**
     * Neighbours of agent will be his team members.
     *
     * @param maxDistance In order to consider a neighbour inside the
     * neighbourhood
     * @param maxAngle In order to consider a neighbour inside the neighbourhood
     * Neighbours of agent will be his team members.
     * @param spatial active spatial during excecution of behavior
     * @param agent
     */
    public AlignmentBehavior(Agent agent, float maxDistance, float maxAngle, Spatial spatial) {
        super(agent, spatial);
        try {
            this.validateMaxDistance(maxDistance);
            this.maxDistance = maxDistance;
            this.maxAngle = maxAngle;
            neighbours = convertToGameEntities(agent.getTeam().getMembers());
        } catch (NullPointerException npe) {
            throw new AgentExceptions.TeamNotFoundException(agent);
        }
    }

    /**
     * maxAngle is setted to PI / 2 by default and maxDistance to infinite.
     *
     * @param agent To whom behavior belongs.
     * @param neighbours Neighbours, this agent is moving toward the center of
     * this neighbours.
     */
    public AlignmentBehavior(Agent agent, List<GameEntity> neighbours) {
        super(agent);
        this.neighbours = neighbours;
    }

    /**
     * @param maxDistance In order to consider a neighbour inside the
     * neighbourhood
     * @param maxAngle In order to consider a neighbour inside the neighbourhood
     *
     * @throws NegativeMaxDistanceException If maxDistance is lower than 0
     *
     * @see Agent#inBoidNeighborhoodMaxAngle(com.jme3.ai.agents.Agent, float,
     * float, float)
     * @see AlignmentBehavior#AlignmentBehavior(com.jme3.ai.agents.Agent,
     * java.util.List)
     */
    public AlignmentBehavior(Agent agent, List<GameEntity> neighbours, float maxDistance, float maxAngle) {
        super(agent);
        this.validateMaxDistance(maxDistance);
        this.neighbours = neighbours;
        this.maxDistance = maxDistance;
        this.maxAngle = maxAngle;
    }

    /**
     * @param spatial active spatial during excecution of behavior
     * @see AlignmentBehavior#AlignmentBehavior(com.jme3.ai.agents.Agent,
     * java.util.List)
     */
    public AlignmentBehavior(Agent agent, List<GameEntity> neighbours, Spatial spatial) {
        super(agent, spatial);
        this.neighbours = neighbours;
    }

    /**
     * @see AlignmentBehavior#AlignmentBehavior(com.jme3.ai.agents.Agent,
     * java.util.List)
     * @see AlignmentBehavior#AlignmentBehavior(com.jme3.ai.agents.Agent,
     * java.util.List, float, float)
     */
    public AlignmentBehavior(Agent agent, List<GameEntity> neighbours, float maxDistance, float maxAngle, Spatial spatial) {
        super(agent, spatial);
        this.validateMaxDistance(maxDistance);
        this.neighbours = neighbours;
        this.maxDistance = maxDistance;
        this.maxAngle = maxAngle;
    }

    private void validateMaxDistance(float maxDistance) {
        if (maxDistance < 0) {
            throw new SteeringExceptions.NegativeValueException("The max distance value can not be negative.", maxDistance);
        }
    }

    /**
     * @see AbstractSteeringBehaviour#calculateSteering()
     */
    @Override
    protected Vector3f calculateRawSteering() {
        // steering accumulator and count of neighbors, both initially zero
        Vector3f steering = new Vector3f();
        int realNeighbors = 0;
        // for each of the other vehicles...
        for (GameEntity gameEntity : neighbours) {
            if (this.agent.inBoidNeighborhood(gameEntity, this.agent.getRadius() * 3, this.maxDistance, this.maxAngle)) {
                // accumulate sum of neighbor's positions
                steering = steering.add(gameEntity.fordwardVector());
                realNeighbors++;
            }
        }
        // divide by neighbors, subtract off current position to get error-correcting direction
        if (realNeighbors > 0) {
            steering = steering.divide(realNeighbors);
            steering = this.agent.offset(steering);
        }
        return steering;
    }

    public void setNeighbours(List<GameEntity> neighbours) {
        this.neighbours = neighbours;
    }

    public void setNeighboursFromTeam(Team team) {
        this.neighbours = convertToGameEntities(team.getMembers());
    }
    
    
}