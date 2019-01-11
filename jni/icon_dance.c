#include "snw_test_IconDance.h"
#include <windows.h>
#include <commctrl.h>

HWND GetDesktopListViewHWND()
{
  HWND hDesktopListView = NULL;
  HWND hWorkerW = NULL;

  HWND hProgman = FindWindow("Progman", 0);
  HWND hDesktopWnd = GetDesktopWindow();

  // If the main Program Manager window is found
  if (hProgman)
  {
	// Get and load the main List view window containing the icons (found using Spy++).
    HWND hShellViewWin = FindWindowEx(hProgman, 0, "SHELLDLL_DefView", 0);
    if (hShellViewWin)
      hDesktopListView = FindWindowEx(hShellViewWin, 0, "SysListView32", 0);
	else
		// When this fails (happens in Windows-7 when picture rotation is turned ON), then look for the WorkerW windows list to get the
		// correct desktop list handle.
		// As there can be multiple WorkerW windows, so iterate through all to get the correct one
		do
		{
			hWorkerW = FindWindowEx( hDesktopWnd, hWorkerW, "WorkerW", NULL );
			hShellViewWin = FindWindowEx(hWorkerW, 0, "SHELLDLL_DefView", 0);
		} while (hShellViewWin == NULL && hWorkerW != NULL);

		// Get the ListView control
		hDesktopListView = FindWindowEx(hShellViewWin, 0, "SysListView32", 0);
  }

  return hDesktopListView;
}

JNIEXPORT jint JNICALL Java_snw_test_IconDance_getDesktopHWND
  (JNIEnv *env, jclass thisClass) {
    return  (void*) GetDesktopListViewHWND();
}

JNIEXPORT jint JNICALL Java_snw_test_IconDance_getIconNum
  (JNIEnv *env, jclass thisClass, jint hwnd) {
    return ListView_GetItemCount((void*) hwnd);
}

JNIEXPORT void JNICALL Java_snw_test_IconDance_setIconPos
  (JNIEnv *env, jclass thisClass, jint hwnd, jint id, jint x, jint y) {
    ListView_SetItemPosition (
        (void*) hwnd,
        id,
        x,
        y
    );
}

JNIEXPORT jintArray JNICALL Java_snw_test_IconDance_getSize
  (JNIEnv *env, jclass thisClass, jint hwnd) {
    jintArray size = (jintArray)(*env)->NewIntArray(env, 2);
    jint* size_arr = (*env)->GetIntArrayElements(env, size, NULL);
    size_arr[0] = size_arr[1] = 0;
    RECT rect;
    if(GetWindowRect((void*) hwnd, &rect)) {
        size_arr[0] = rect.right - rect.left;
        size_arr[1] = rect.bottom - rect.top;
        (*env)->ReleaseIntArrayElements(env, size, size_arr, 0);
    }

    return size;
}
