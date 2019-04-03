import time


def wait(seconds_before=0, seconds_after=0):

    def decorator(func):

        def wrapper(*args, **kwargs):
            time.sleep(seconds_before)
            ret = func(*args, **kwargs)
            time.sleep(seconds_after)
            return ret

        return wrapper

    return decorator
