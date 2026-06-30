export default defineNuxtRouteMiddleware((to) => {
  const auth = useAuthStore();
  auth.loadFromStorage();

  if (!auth.isAuthenticated) {
    return navigateTo({ path: "/login", query: { redirect: to.fullPath } });
  }

  const requiredRoles = to.meta.roles as string[] | undefined;
  if (requiredRoles?.length && !auth.hasAnyRole(requiredRoles)) {
    return navigateTo("/");
  }
});
