# sdp-final-temp-1

Sergey Kim 
Tilekkali Nurtore
Nurkhan Karashev
SE-2540
# Karashev Nurkhan

## Project Overview

Abyss Outpost is a group project developed in Java using design patterns.

The project is a submarine exploration game where players can choose different diving strategies, explore underwater locations, collect rewards, and face dangers.

Our team uses different design patterns to organize the code and make it easier to maintain and extend.

## My Contribution — Strategy and Observer

I was responsible for implementing two design patterns:

1. Strategy Pattern
2. Observer Pattern

I worked on the `feature/strategy-observer` branch and created the required classes and a demo to test their functionality.

## 1. Strategy Pattern

The Strategy pattern allows the player to choose different diving tactics without changing the main game logic.

I implemented three strategies:

- **CautiousTactic** — uses less oxygen, reduces damage, but gives fewer rewards.
- **GreedyTactic** — uses more oxygen, increases damage and rewards.
- **StealthTactic** — provides a higher success bonus for sneaking.

All three classes implement the `DiveTactic` interface.

The interface contains five methods:

- `name()`
- `oxygenPerTurn()`
- `successBonus(Action action)`
- `damageMultiplier()`
- `lootMultiplier()`

Each strategy provides different values.

## 2. Observer Pattern

The Observer pattern is used to notify different parts of the game when an event happens.

I implemented five classes:

- **EventBus** — manages subscribers and sends events.
- **LogSubscriber** — collects messages for the game log.
- **OxygenMonitor** — checks oxygen levels and creates warnings.
- **HullMonitor** — checks submarine hull condition.
- **DepthMonitor** — detects when the submarine goes beyond its safe depth.

All monitors and the log subscriber implement the `Subscriber` interface.

The `EventBus` sends events to all registered subscribers.

### Warning Conditions

OxygenMonitor:
- Warning when oxygen is 30% or lower.
- Critical warning when oxygen is 15% or lower.

HullMonitor:
- Warning when hull integrity is 40% or lower.
- Critical warning when hull integrity is 20% or lower.

DepthMonitor:
- Sends one critical warning when depth exceeds the submarine's rated depth.

## 3. Testing

I created `P2Demo.java` to check both design patterns.

The demo tests:

- Three different diving strategies.
- Oxygen warnings.
- Hull warnings.
- Depth warnings.
- EventBus notifications.
- LogSubscriber event collection.

The program successfully produced five alerts in the expected order.

The second `drain()` returned zero events, showing that the log was cleared correctly.

All three strategies returned the expected values.

## 4. Git and GitHub

I worked in a separate branch:

`feature/strategy-observer`

I used Git commits to track my implementation steps.

After completing the implementation, I pushed my branch to GitHub and created a Pull Request for team integration.

**Pull Request:** https://github.com/dar-key/sdp-final-temp-1/pull/3

## 5. UML Diagrams

I created two UML class diagrams:

UML — Strategy:

![Strategy UML Diagram](umldiagramma/image.png)

UML - Observer:
![Observer UML Diagram](umldiagramma/image-1.png)

These diagrams show the relationships between the interfaces and their implementations.

## 6. Conclusion

In this group project, I implemented the Strategy and Observer patterns.

Strategy makes it possible to change diving tactics easily.

Observer allows the game to send events and warnings without directly connecting every component.

Both patterns help make the game more flexible and easier to extend.

My implementation was tested using `P2Demo.java` and submitted through a GitHub Pull Request.