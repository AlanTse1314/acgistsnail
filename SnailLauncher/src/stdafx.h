// stdafx.h����׼ϵͳ�����ļ��İ����ļ����Ǿ���ʹ�õ��������ĵ��ض�����Ŀ�İ����ļ�
//

#pragma once

#ifndef VC_EXTRALEAN
#define VC_EXTRALEAN // �� Windows ͷ���ų�����ʹ�õ�����
#endif

#include "targetver.h"

#define _ATL_CSTRING_EXPLICIT_CONSTRUCTORS // ĳЩ CString ���캯��������ʽ��

#define _AFX_ALL_WARNINGS // �ر� MFC ��ĳЩ�����������ɷ��ĺ��Եľ�����Ϣ������

#include <afxwin.h> // MFC ��������ͱ�׼���
#include <afxext.h> // MFC ��չ

#ifndef _AFX_NO_OLE_SUPPORT
#include <afxdtctl.h> // MFC �� Internet Explorer 4 �����ؼ���֧��
#endif
#ifndef _AFX_NO_AFXCMN_SUPPORT
#include <afxcmn.h> // MFC �� Windows �����ؼ���֧��
#endif

#include <afxcontrolbars.h> // �������Ϳؼ����� MFC ֧��
