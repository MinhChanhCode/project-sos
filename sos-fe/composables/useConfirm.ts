import { reactive, ref } from "vue";

type ConfirmOptions = {
  title?: string;
  message?: string;
  confirmText?: string;
  cancelText?: string;
  destructive?: boolean;
};

const isOpen = ref(false);
const options = reactive<Required<ConfirmOptions>>({
  title: "Xác nhận",
  message: "Bạn có chắc chắn?",
  confirmText: "Xác nhận",
  cancelText: "Hủy",
  destructive: false,
});

let resolver: ((value: boolean) => void) | null = null;

export const useConfirm = () => {
  const open = (opts: ConfirmOptions = {}): Promise<boolean> => {
    options.title = opts.title ?? "Xác nhận";
    options.message = opts.message ?? "Bạn có chắc chắn?";
    options.confirmText = opts.confirmText ?? "Xác nhận";
    options.cancelText = opts.cancelText ?? "Hủy";
    options.destructive = opts.destructive ?? false;
    isOpen.value = true;
    return new Promise<boolean>((resolve) => {
      resolver = resolve;
    });
  };

  const confirm = () => {
    isOpen.value = false;
    if (resolver) resolver(true);
    resolver = null;
  };

  const cancel = () => {
    isOpen.value = false;
    if (resolver) resolver(false);
    resolver = null;
  };

  return {
    // state
    isOpen,
    options,
    // actions
    open,
    confirm,
    cancel,
  };
};
