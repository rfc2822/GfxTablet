#!/usr/bin/env python3
import subprocess
import pyautogui
from collections import defaultdict


INTMAX = 65536
WIDTH, HEIGHT = pyautogui.size()
ASPECT_RATIOS = defaultdict(lambda: (16, 9))
default_ratios = [
    ("Galaxy Note 4", (16, 9)),
]


class Event:

    def __str__(self):
        return "{}({})".format(
            self.__class__.__name__,
            ', '.join(
                ["{}={}".format(k, v) for k, v in self.__dict__.items()]))


class PositionEvent(Event):
    """Model of position events."""

    def __init__(self, x, y, pressure):
        super().__init__()
        self.x = x
        self.y = y
        self.pressure = pressure


class ButtonEvent(Event):
    """Model of button events."""

    def __init__(self, button_id, button_status):
        super().__init__()
        self.id = button_id
        self.status = button_status


class PositionManager:
    """Model for handling relative vs. absolute movement and track button states."""

    def __init__(self, device, aspect_ratio=None):
        if aspect_ratio is None:
            try:
                r = ASPECT_RATIOS[device]
                self.xscale = r[0] / r[1]
            except KeyError:
                raise ValueError("Sorry, that is an unsupported device.")
        self.last_pos_event = None
        self._deltas = [0, 0]
        # only track movement delta when btn id -1 state is 1
        self.button_states = defaultdict(lambda: 0)

    def consume(self, event):
        if isinstance(event, PositionEvent):
            if self.last_pos_event is not None:
                # skip non-relative movement events
                if self.button_states.get(-1, None) == 1:
                    self._deltas[0] = (
                        event.x - self.last_pos_event.x) * self.xscale * HEIGHT / INTMAX
                    self._deltas[1] = (
                        event.y - self.last_pos_event.y) * HEIGHT / INTMAX
            self.last_pos_event = event
        elif isinstance(event, ButtonEvent):
            self.button_states[event.id] = event.status
        return tuple(self._deltas)

    @property
    def deltas(self):
        return tuple(self._deltas)


def process_line(l):
    if l.startswith("."):
        return PositionEvent(*[int(i.split(":")[1].strip()) for i in l.split(",")])
    elif l.startswith("sent button"):
        ss = l.split(":")[1].strip()
        return ButtonEvent(*[int(i.strip()) for i in ss.split(",")])
    else:
        raise ValueError("Invalid command string: {}".format(l))


def main():
    # necessary to prevent pyautogui calls from causing unnecessary delays
    pyautogui.PAUSE = 0
    cmd = ["../driver-uinput/networktablet"]
    try:
        p = subprocess.Popen(cmd, stdout=subprocess.PIPE)
    except FileNotFoundError:
        print("ERROR: Did you build the binary?")
        quit()

    movement_manager = PositionManager("Galaxy Note 4")
    while True:
        try:
            line = p.stdout.readline().decode("utf-8")
            if not line:
                break
            try:
                e = process_line(line)
                print(e)
                movement_manager.consume(e)
                dx, dy = movement_manager.deltas
                pyautogui.moveRel(dx, dy, 0)

                if movement_manager.button_states.get(0, None) == 1:
                    pyautogui.mouseDown(button="left")
                else:
                    pyautogui.mouseUp(button="left")
            except ValueError:
                pass
        except KeyboardInterrupt:
            quit()


if __name__ == '__main__':
    main()
