import ACCESS_ENUM from '@/access/accessEnum'

/**
 * 检查权限（判断当前登录用户是否具有某个权限）
 * @param loginUser 当前登录用户
 * @param needAccess 需要有的权限
 * @return boolean 有无权限
 */
const checkAccess = (loginUser: API.LoginUserVO, needAccess = ACCESS_ENUM.NOT_LOGIN) => {
  const loginUserAccess = loginUser?.userRole ?? ACCESS_ENUM.NOT_LOGIN

  // 权限等级映射：NOT_LOGIN < USER < ADMIN
  const accessLevel = {
    [ACCESS_ENUM.NOT_LOGIN]: 0,
    [ACCESS_ENUM.USER]: 1,
    [ACCESS_ENUM.ADMIN]: 2,
  }

  // 直接比较权限等级
  return accessLevel[loginUserAccess] >= accessLevel[needAccess]
}

export default checkAccess
